#pragma once

#include <algorithm>
#include <cstdint>
#include <cstring>
#include <functional>
#include <iomanip>
#include <iostream>
#include <memory>
#include <random>
#include <sstream>
#include <stdexcept>
#include <vector>

template <class Key, class Value, std::size_t BlockSize = 4096, class Less = std::less<Key>>
class BPTree
{
public:
    static const size_t K = std::max(static_cast<int>((BlockSize - sizeof(std::size_t) - 2 * sizeof(void *)) / (sizeof(Key) + sizeof(void *))), 7);

private:
    class Node
    {
    public:
        virtual bool isLeaf() = 0;

        virtual void setParent(Node *) = 0;

        virtual Key & getKey(size_t idx) = 0;

        virtual size_t getSize() const = 0;

        virtual ~Node() = default;
    };

    class Internal : public Node
    {
    public:
        Internal(std::array<Key, K> & nkeys, std::array<Node *, K + 1> & nchildren, size_t nsize, size_t start)
            : size(nsize)
        {
            for (size_t i = 0; i < size - 1; ++i) {
                children[i] = nchildren[i + start];
                keys[i] = nkeys[i + start];
            }
            children[size - 1] = nchildren[size + start - 1];
        }

        Internal(const Key & key, Node * left, Node * right)
            : size(2)
        {
            keys[0] = key;
            children[0] = left;
            children[1] = right;
        }

        bool isLeaf() override
        {
            return false;
        };

        size_t getSize() const override
        {
            return size;
        }

        Internal * getParent()
        {
            return parent;
        }

        void setParent(Node * nparent) override
        {
            parent = static_cast<Internal *>(nparent);
        }

        Node * getChildren(size_t idx)
        {
            return children[idx];
        }

        void setChildren(size_t idx, Node * nchildren)
        {
            children[idx] = nchildren;
        }

        std::array<Node *, K + 1> & getChildrens()
        {
            return children;
        }

        void setKey(size_t idx, Key nkey)
        {
            keys[idx] = nkey;
        }

        Key & getKey(size_t idx) override
        {
            return keys[idx];
        }

        std::array<Key, K> & getKeys()
        {
            return keys;
        }

        void addToSize()
        {
            size++;
        }

        void minusToSize()
        {
            size--;
        }

        Internal * getLeftSibling(const Key & key)
        {
            if (parent == nullptr) {
                return nullptr;
            }
            if (Less{}(key, getParent()->getKey(0))) {
                return nullptr;
            }
            size_t i = 0;
            while (i < getParent()->getSize() - 1 && Less{}(getParent()->getKey(i), key)) {
                ++i;
            }
            return static_cast<Internal *>(getParent()->getChildren(i - 1));
        }

        Internal * getRightSibling(const Key & key)
        {
            if (parent == nullptr) {
                return nullptr;
            }
            size_t i = 1;
            while ((i == 1 || i < getParent()->getSize()) && Less{}(getParent()->getKey(i - 1), key)) {
                ++i;
            }
            if (i > getParent()->getSize() - 1) {
                return nullptr;
            }
            return static_cast<Internal *>(getParent()->getChildren(i));
        }

        ~Internal() = default;

    private:
        Internal * parent = nullptr;
        std::array<Key, K> keys;
        std::array<Node *, K + 1> children;
        size_t size = 0;
    };

    class Leaf : public Node
    {
    public:
        Leaf() = default;

        Leaf(const Leaf &) = default;

        Leaf(std::array<std::pair<Key, Value>, K> & ndata, size_t nsize, size_t start)
            : size(nsize)
        {
            for (size_t i = 0; i < size; ++i) {
                data[i] = std::make_pair(ndata[i + start].first, std::move(ndata[i + start].second));
            }
        }

        bool isLeaf() override
        {
            return true;
        };

        size_t getSize() const override
        {
            return size;
        }

        Internal * getParent()
        {
            return parent;
        }

        void setParent(Node * nparent) override
        {
            parent = static_cast<Internal *>(nparent);
        }

        Key & getKey(size_t idx) override
        {
            return data[idx].first;
        }

        void setKey(size_t idx, const Key & key)
        {
            data[idx].first = key;
        }

        std::array<std::pair<Key, Value>, K> & getData()
        {
            return data;
        }

        std::pair<Key, Value> & getIdxData(size_t idx)
        {
            return data[idx];
        }

        Value && getValueR(size_t idx)
        {
            return std::move(data[idx].second);
        }

        Value & getValueL(size_t idx)
        {
            return data[idx].second;
        }

        void setValue(size_t idx, Value && val)
        {
            data[idx].second = std::move(val);
        }

        void setValue(size_t idx, const Value & val)
        {
            data[idx].second = val;
        }

        Leaf * getNextLeaf()
        {
            return nextLeaf;
        }

        void setNextLeaf(Leaf * nleaf)
        {
            nextLeaf = nleaf;
        }

        void addToSize()
        {
            size++;
        }

        void minusToSize()
        {
            size--;
        }

        bool operator==(const Leaf & other) const
        {
            return (this->data == other.data);
        }

        Leaf * getLeftSibling(const Key & key)
        {
            if (parent == nullptr) {
                return nullptr;
            }
            Internal * begin = getParent();
            while (begin->getParent() != nullptr) {
                begin = begin->getParent();
            }
            Node * start = static_cast<Node *>(begin);
            while (!start->isLeaf()) {
                start = static_cast<Internal *>(start)->getChildren(0);
            }
            Key minkey = static_cast<Leaf *>(start)->getKey(0);
            if (!Less{}(key, minkey) && !Less{}(minkey, key)) {
                return nullptr;
            }
            start = static_cast<Node *>(begin);
            while (!start->isLeaf()) {
                auto * internal = static_cast<Internal *>(start);
                for (size_t i = 0; i < internal->getSize(); ++i) {
                    if (i == internal->getSize() - 1 || !Less{}(internal->getKey(i), key)) {
                        start = internal->getChildren(i);
                        break;
                    }
                }
            }
            return static_cast<Leaf *>(start);
        }

        Leaf * getRightSibling()
        {
            if (nextLeaf == nullptr) {
                return nullptr;
            }
            if (!Less{}(nextLeaf->getParent()->getKey(0), getParent()->getKey(0)) && !Less{}(getParent()->getKey(0), nextLeaf->getParent()->getKey(0))) {
                return nextLeaf;
            }
            return nullptr;
        }

        ~Leaf() = default;

    private:
        Internal * parent = nullptr;
        Leaf * nextLeaf = nullptr;
        std::array<std::pair<Key, Value>, K> data;
        size_t size = 0;
    };

    Node * root = nullptr;
    size_t treeSize = 0;

public:
    using key_type = Key;
    using mapped_type = Value;
    using value_type = std::pair<Key, Value>;
    using reference = value_type &;
    using const_reference = const value_type &;
    using pointer = value_type *;
    using const_pointer = const value_type *;
    using size_type = std::size_t;

    class iterator;

    class const_iterator
    {
    public:
        using iterator_category = std::forward_iterator_tag;
        using difference_type = std::ptrdiff_t;
        using pointer = const std::pair<Key, Value> *;
        using reference = const std::pair<Key, Value> &;
        using value_type = const std::pair<Key, Value>;

        const_iterator()
            : currentLeaf(nullptr)
            , currentIndex(0)
        {
        }

        const_iterator(Leaf * leaf, size_t size)
            : currentLeaf(leaf)
            , currentIndex(size)
        {
        }

        const_iterator(const iterator & it)
        {
            const_iterator(it.getCurrentLeaf(), it.getCurrentIndex());
        }

        reference operator*() const
        {
            return currentLeaf->getIdxData(currentIndex);
        }

        pointer operator->() const
        {
            return &currentLeaf->getIdxData(currentIndex);
        }

        const_iterator operator++(int)
        {
            const_iterator it = *this;
            ++(*this);
            return it;
        }

        const_iterator & operator++()
        {
            if (currentIndex + 1 >= currentLeaf->getSize()) {
                currentLeaf = currentLeaf->getNextLeaf();
                currentIndex = 0;
            }
            else {
                currentIndex++;
            }
            return *this;
        }

        bool operator==(const const_iterator & x) const
        {
            return (x.currentLeaf == currentLeaf) && (x.currentIndex == currentIndex);
        }

        bool operator!=(const const_iterator & x) const
        {
            return (x.currentLeaf != currentLeaf) || (x.currentIndex != currentIndex);
        }

        Leaf * getCurrentLeaf() const
        {
            return currentLeaf;
        }

        size_t getCurrentIndex() const
        {
            return currentIndex;
        }

    private:
        Leaf * currentLeaf = nullptr;
        size_t currentIndex = 0;
    };

    class iterator
    {
    public:
        using iterator_category = std::forward_iterator_tag;
        using difference_type = std::ptrdiff_t;
        using pointer = std::pair<Key, Value> *;
        using reference = std::pair<Key, Value> &;
        using value_type = std::pair<Key, Value>;

        iterator()
            : currentLeaf(nullptr)
            , currentIndex(0)
        {
        }

        iterator(Leaf * leaf, size_t size)
            : currentLeaf(leaf)
            , currentIndex(size)
        {
        }

        reference operator*() const
        {
            return currentLeaf->getIdxData(currentIndex);
        }

        pointer operator->() const
        {
            return &currentLeaf->getIdxData(currentIndex);
        }

        iterator operator++(int)
        {
            iterator it = *this;
            ++(*this);
            return it;
        }

        iterator & operator++()
        {
            if (currentIndex + 1 >= currentLeaf->getSize()) {
                currentLeaf = currentLeaf->getNextLeaf();
                currentIndex = 0;
            }
            else {
                currentIndex++;
            }
            return *this;
        }

        bool operator==(const iterator & x) const
        {
            return (x.currentLeaf == currentLeaf) && (x.currentIndex == currentIndex);
        }

        bool operator==(const const_iterator & x) const
        {
            return (x.getCurrentLeaf() == currentLeaf) && (x.getCurrentIndex() == currentIndex);
        }

        bool operator!=(const iterator & x) const
        {
            return (x.currentLeaf != currentLeaf) || (x.currentIndex != currentIndex);
        }

        bool operator!=(const const_iterator & x) const
        {
            return (x.getCurrentLeaf() != currentLeaf) || (x.getCurrentIndex() != currentIndex);
        }

        Leaf * getCurrentLeaf() const
        {
            return currentLeaf;
        }

        size_t getCurrentIndex() const
        {
            return currentIndex;
        }

    private:
        Leaf * currentLeaf = nullptr;
        size_t currentIndex = 0;
    };

    BPTree(std::initializer_list<std::pair<Key, Value>> list)
        : root(static_cast<Leaf *>(new Leaf()))
    {
        insert(list);
    };

    BPTree(const BPTree & other)
        : root(static_cast<Leaf *>(new Leaf()))
    {
        insert(other.begin(), other.end());
    };

    BPTree(BPTree && other)
    {
        clear();
        std::swap(treeSize, other.treeSize);
        std::swap(root, other.root);
    };

    BPTree()
        : root(static_cast<Leaf *>(new Leaf()))
        , treeSize(0)
    {
    }

    BPTree & operator=(BPTree && other)
    {
        if (this == &other) {
            return *this;
        }
        clear();
        std::swap(treeSize, other.treeSize);
        std::swap(root, other.root);
        return *this;
    }

    BPTree & operator=(const BPTree & other)
    {
        if (this == &other) {
            return *this;
        }
        clear();
        root = static_cast<Node *>(new Leaf());
        insert(other.begin(), other.end());
        return *this;
    }

    bool operator==(const BPTree & other) const
    {
        if (this == &other) {
            return true;
        }
        if (treeSize != other.treeSize) {
            return false;
        }
        iterator beginThis = begin();
        iterator beginOther = other.begin();
        for (; beginThis != end(); ++beginThis) {
            if (*beginThis != *beginOther) {
                return false;
            }
            ++beginOther;
        }
        return true;
    }

    bool operator!=(const BPTree & other) const { return !operator==(other); }

    iterator begin()
    {
        if (this->treeSize == 0) {
            return end();
        }
        Node * begin = root;
        while (!begin->isLeaf()) {
            begin = static_cast<Internal *>(begin)->getChildren(0);
        }
        return iterator(static_cast<Leaf *>(begin), 0);
    }

    const_iterator cbegin() const
    {
        if (treeSize == 0) {
            return cend();
        }
        Node * begin = root;
        while (!begin->isLeaf()) {
            begin = static_cast<Internal *>(begin)->getChildren(0);
        }
        return const_iterator(static_cast<Leaf *>(begin), 0);
    };

    const_iterator begin() const
    {
        return cbegin();
    };

    iterator end()
    {
        return iterator(nullptr, 0);
    };

    const_iterator cend() const
    {
        return const_iterator(nullptr, 0);
    };

    const_iterator end() const
    {
        return cend();
    };

    bool empty() const
    {
        return treeSize == 0;
    };

    size_type size() const
    {
        return treeSize;
    };

    size_type count(const Key & key) const
    {
        return contains(key) ? 1 : 0;
    };

    bool contains(const Key & key) const
    {
        return find(key) != end();
    };

    BPTree & operator++()
    {
        ++treeSize;
        return *this;
    }

    Leaf * findLeaf(const Key & key) const
    {
        Node * begin = root;
        while (!begin->isLeaf()) {
            Internal * internal = static_cast<Internal *>(begin);
            for (size_t i = 0; i < internal->getSize(); ++i) {
                if (i == internal->getSize() - 1 || Less{}(key, internal->getKey(i))) {
                    begin = internal->getChildren(i);
                    break;
                }
            }
        }
        return static_cast<Leaf *>(begin);
    }

    iterator find(const Key & key)
    {
        if (root == nullptr) {
            return end();
        }
        Leaf * leaf = findLeaf(key);
        if (leaf == nullptr) {
            return end();
        }
        size_t i = 0;
        while (i != leaf->getSize()) {
            if (!Less{}(leaf->getKey(i), key) && !Less{}(key, leaf->getKey(i))) {
                return iterator(leaf, i);
            }
            ++i;
        }
        return end();
    }

    const_iterator find(const Key & key) const
    {
        if (root == nullptr) {
            return cend();
        }
        Leaf * leaf = findLeaf(key);
        size_t i = 0;
        while (i != leaf->getSize()) {
            if (!Less{}(leaf->getKey(i), key) && !Less{}(key, leaf->getKey(i))) {
                return const_iterator(leaf, i);
            }
            ++i;
        }
        return cend();
    }

    iterator lower_bound(const Key & key)
    {
        if (end() == begin()) {
            return end();
        }
        if (Less{}(key, begin()->first)) {
            return begin();
        }
        size_t i = 0;
        Leaf * leaf = findLeaf(key);
        while (i != leaf->getSize()) {
            if (!Less{}(leaf->getKey(i), key)) {
                return iterator(leaf, i);
            }
            ++i;
        }
        return iterator(leaf->getNextLeaf(), 0);
    };

    iterator upper_bound(const Key & key)
    {
        iterator it = lower_bound(key);
        if (it == end()) {
            return end();
        }
        if (it == begin() && Less{}(key, begin()->first)) {
            return begin();
        }
        it++;
        return it;
    };

    const_iterator lower_bound(const Key & key) const
    {
        if (cend() == cbegin()) {
            return cend();
        }
        if (Less{}(key, begin()->first)) {
            return cbegin();
        }
        size_t i = 0;
        Leaf * leaf = findLeaf(key);
        while (i != leaf->getSize()) {
            if (!Less{}(leaf->getKey(i), key)) {
                return const_iterator(leaf, i);
            }
            ++i;
        }
        return const_iterator(leaf->getNextLeaf(), 0);
    };

    const_iterator upper_bound(const Key & key) const
    {
        const_iterator it = lower_bound(key);
        if (it == cend()) {
            return cend();
        }
        if (it == cbegin() && Less{}(key, cbegin()->first)) {
            return cbegin();
        }
        it++;
        return it;
    };

    std::pair<iterator, iterator> equal_range(const Key & key)
    {
        return std::make_pair(lower_bound(key), upper_bound(key));
    };

    std::pair<const_iterator, const_iterator> equal_range(const Key & key) const
    {
        return std::make_pair(lower_bound(key), upper_bound(key));
    };

    Value & at(const Key & key)
    {
        if (find(key) == end()) {
            throw std::out_of_range("Iterator out of range");
        }
        else {
            iterator it = find(key);
            return it->second;
        }
    };

    const Value & at(const Key & key) const
    {
        if (find(key) == end()) {
            throw std::out_of_range("Iterator out of range");
        }
        else {
            const_iterator it = find(key);
            return it->second;
        }
    };

    Value & operator[](const Key & key)
    {
        iterator it = find(key);
        if (it != end()) {
            return it->second;
        }
        else {
            return insert(key, Value()).first->second;
        }
    };

    std::pair<iterator, bool> splitRootLeaf(size_t idx)
    {
        Leaf * leafRoot = static_cast<Leaf *>(root);
        Leaf * left = new Leaf(leafRoot->getData(), leafRoot->getSize() / 2, 0);
        Leaf * right = new Leaf(leafRoot->getData(), leafRoot->getSize() - leafRoot->getSize() / 2, leafRoot->getSize() / 2);
        Internal * newRoot = new Internal(leafRoot->getKey(leafRoot->getSize() / 2), static_cast<Leaf *>(left), static_cast<Leaf *>(right));
        root = static_cast<Node *>(newRoot);
        left->setParent(root);
        right->setParent(root);
        size_t i = 0;
        while (i < left->getParent()->getSize() - 1 && Less{}(left->getParent()->getKey(i), right->getKey(0))) {
            ++i;
        }
        if (i != 0) {
            static_cast<Leaf *>(left->getParent()->getChildren(i - 1))->setNextLeaf(left);
        }
        left->setNextLeaf(right);
        right->setNextLeaf(leafRoot->getNextLeaf());
        delete leafRoot;
        if (left->getSize() > idx) {
            return std::make_pair(iterator(left, idx), true);
        }
        else {
            return std::make_pair(iterator(right, idx - left->getSize()), true);
        }
    }

    std::pair<iterator, bool> splitLeaf(Leaf * leaf, size_t idx)
    {
        if (leaf->getParent() == nullptr) {
            return splitRootLeaf(idx);
        }
        else {
            Leaf * left = new Leaf(leaf->getData(), leaf->getSize() / 2, 0);
            Leaf * right = new Leaf(leaf->getData(), leaf->getSize() - leaf->getSize() / 2, leaf->getSize() / 2);
            left->setNextLeaf(right);
            right->setNextLeaf(leaf->getNextLeaf());
            Leaf * prevLeaf = leaf->getLeftSibling(leaf->getKey(0));
            if (prevLeaf != nullptr) {
                prevLeaf->setNextLeaf(left);
            }
            right->setParent(static_cast<Node *>(leaf->getParent()));
            left->setParent(static_cast<Node *>(leaf->getParent()));
            size_t i = 0;
            delete leaf;
            while (i < left->getParent()->getSize() - 1 && Less{}(left->getParent()->getKey(i), right->getKey(0))) {
                ++i;
            }
            left->getParent()->setChildren(i, left);
            insertInInternal(left->getParent(), right->getKey(0), right);
            if (left->getSize() > idx) {
                return std::make_pair(iterator(left, idx), true);
            }
            else {
                return std::make_pair(iterator(right, idx - left->getSize()), true);
            }
        }
    }

    std::pair<iterator, bool> insertInLeaf(const Key & key, Value && value)
    {
        if (contains(key)) {
            return std::make_pair(end(), false);
        }
        treeSize++;
        Leaf * leaf = findLeaf(key);
        if (leaf == nullptr) {
            return std::make_pair(end(), false);
        }
        size_t i = 0;
        while (i < leaf->getSize() && Less{}(leaf->getKey(i), key)) {
            ++i;
        }
        for (size_t j = leaf->getSize(); j > i; --j) {
            leaf->setKey(j, leaf->getKey(j - 1));
            leaf->setValue(j, leaf->getValueR(j - 1));
        }
        leaf->setKey(i, key);
        leaf->setValue(i, std::move(value));
        leaf->addToSize();
        if (leaf->getSize() == K) {
            return splitLeaf(leaf, i);
        }
        else {
            return std::make_pair(iterator(leaf, i), true);
        }
    }

    std::pair<iterator, bool> insertInLeaf(const Key & key, const Value & value)
    {
        if (contains(key)) {
            return std::make_pair(end(), false);
        }
        treeSize++;
        Leaf * leaf = findLeaf(key);
        if (leaf == nullptr) {
            return std::make_pair(end(), false);
        }
        size_t i = 0;
        while (i < leaf->getSize() && Less{}(leaf->getKey(i), key)) {
            ++i;
        }
        for (size_t j = leaf->getSize(); j > i; --j) {
            leaf->setKey(j, leaf->getKey(j - 1));
            leaf->setValue(j, leaf->getValueL(j - 1));
        }
        leaf->setKey(i, key);
        leaf->setValue(i, value);
        leaf->addToSize();
        if (leaf->getSize() == K) {
            return splitLeaf(leaf, i);
        }
        else {
            return std::make_pair(iterator(leaf, i), true);
        }
    }

    void insertInInternal(Internal * internal, const Key & key, Node * node)
    {
        size_t i = 0;
        while (i < internal->getSize() - 1 && Less{}(internal->getKey(i), key)) {
            ++i;
        }
        for (size_t j = internal->getSize() - 1; j > i; --j) {
            internal->setKey(j, internal->getKey(j - 1));
            internal->setChildren(j + 1, internal->getChildren(j));
        }
        internal->setKey(i, key);
        internal->setChildren(i + 1, node);
        node->setParent(static_cast<Node *>(internal));
        internal->addToSize();
        if (internal->getSize() == K + 1) {
            splitInternal(internal);
        }
    }

    void splitInternal(Internal * internal)
    {
        if (internal->getParent() == nullptr) {
            splitRootInternal();
        }
        else {
            Internal * left = new Internal(internal->getKeys(), internal->getChildrens(), internal->getSize() / 2, 0);
            Internal * right = new Internal(internal->getKeys(), internal->getChildrens(), (internal->getSize() + 1) / 2, internal->getSize() / 2);
            Key nkey = internal->getKey(internal->getSize() / 2 - 1); // -1 мжт стоит до деления, но тогда и в splitRootInternal тоже
            right->setParent(static_cast<Node *>(internal->getParent()));
            left->setParent(static_cast<Node *>(internal->getParent()));
            size_t i = 0;
            auto tmp = internal->getKey(0);
            delete internal;
            while (i < left->getParent()->getSize() - 1 && Less{}(left->getParent()->getKey(i), tmp)) {
                ++i;
            }
            left->getParent()->setChildren(i, left);
            for (size_t j = 0; j < left->getSize(); ++j) {
                left->getChildren(j)->setParent(static_cast<Node *>(left));
            }
            for (size_t j = 0; j < right->getSize(); ++j) {
                right->getChildren(j)->setParent(static_cast<Node *>(right));
            }
            insertInInternal(left->getParent(), nkey, right);
        }
    }

    void splitRootInternal()
    {
        Internal * internalRoot = static_cast<Internal *>(root);
        Internal * left = new Internal(internalRoot->getKeys(), internalRoot->getChildrens(), internalRoot->getSize() / 2, 0); // Internal(Key *, Node *, size, start)
        Internal * right = new Internal(internalRoot->getKeys(), internalRoot->getChildrens(), (internalRoot->getSize() + 1) / 2, internalRoot->getSize() / 2);
        Internal * newRoot = new Internal(internalRoot->getKey(internalRoot->getSize() / 2 - 1), left, right); // тут -1 может перед делением
        root = static_cast<Node *>(newRoot);
        left->setParent(root);
        right->setParent(root);
        delete internalRoot;
        for (size_t i = 0; i < left->getSize(); ++i) {
            left->getChildren(i)->setParent(static_cast<Node *>(left));
        }
        for (size_t i = 0; i < right->getSize(); ++i) {
            right->getChildren(i)->setParent(static_cast<Node *>(right));
        }
    }

    void recursiveClear(Node * node)
    {
        if (node == nullptr) {
            return;
        }
        if (!node->isLeaf()) {
            for (size_t i = 0; i < static_cast<Internal *>(node)->getSize(); ++i) {
                if (static_cast<Internal *>(node)->getChildren(i) != nullptr) {
                    recursiveClear(static_cast<Internal *>(node)->getChildren(i));
                }
            }
        }
        delete node;
    }

    void clear()
    {
        recursiveClear(root);
        treeSize = 0;
        root = nullptr;
    };

    std::pair<iterator, bool> insert(const Key & key, const Value & value)
    {
        return insertInLeaf(key, value);
    }

    std::pair<iterator, bool> insert(const Key & key, Value && value)
    {
        return insertInLeaf(key, std::move(value));
    }

    template <class ForwardIt>
    void insert(ForwardIt begin, ForwardIt end)
    {
        for (auto i = begin; i != end; ++i) {
            insert(i->first, i->second);
        }
    }

    void insert(std::initializer_list<value_type> list)
    {
        for (auto i = list.begin(); i != list.end(); ++i) {
            insert(i->first, i->second);
        }
    }

    void updatePath(Internal * internal, const Key & oldKey, const Key & newKey)
    {
        if (!Less{}(oldKey, newKey) && !Less{}(newKey, oldKey)) {
            return;
        }
        Internal * node = internal;
        while (node != nullptr) {
            for (size_t i = 0; i < node->getSize() - 1; ++i) {
                if (!Less{}(node->getKey(i), oldKey) && !Less{}(oldKey, node->getKey(i))) {
                    node->setKey(i, newKey);
                    return;
                }
            }
            node = node->getParent();
        }
    }

    void eraseInLeaf(Leaf * leaf, const Key & key)
    {
        auto firstInLeaf = leaf->getKey(0);
        size_t i = 0;
        while (i < leaf->getSize() && Less{}(leaf->getKey(i), key)) {
            ++i;
        }
        for (; i < leaf->getSize() - 1; ++i) {
            leaf->setKey(i, leaf->getKey(i + 1));
            leaf->setValue(i, leaf->getValueR(i + 1));
        }
        leaf->minusToSize();
        updatePath(leaf->getParent(), firstInLeaf, leaf->getKey(0));
        if (leaf->getParent() != nullptr && leaf->getSize() < K / 2) {
            Leaf * leftSibling = leaf->getLeftSibling(leaf->getKey(0));
            Leaf * rightSibling = leaf->getRightSibling();
            if (leftSibling != nullptr && leftSibling->getSize() > K / 2) {
                leftSibling->minusToSize();
                leaf->addToSize();
                Key old = leaf->getKey(0);
                Key oldleft = leftSibling->getKey(0);
                for (size_t j = leaf->getSize() - 1; j > 0; --j) {
                    leaf->setKey(j, leaf->getKey(j - 1));
                    leaf->setValue(j, leaf->getValueR(j - 1));
                }
                leaf->setKey(0, leftSibling->getKey(leftSibling->getSize()));
                leaf->setValue(0, leftSibling->getValueR(leftSibling->getSize()));
                updatePath(leaf->getParent(), old, leaf->getKey(0));
                updatePath(leaf->getParent(), oldleft, leftSibling->getKey(0));
            }
            else if (rightSibling != nullptr && rightSibling->getSize() > K / 2) {
                leaf->addToSize();
                Key old = rightSibling->getKey(0);
                Key oldleaf = leaf->getKey(0);
                leaf->setKey(leaf->getSize() - 1, rightSibling->getKey(0));
                leaf->setValue(leaf->getSize() - 1, rightSibling->getValueR(0));
                for (size_t j = 0; j < rightSibling->getSize() - 1; ++j) {
                    rightSibling->setKey(j, rightSibling->getKey(j + 1));
                    rightSibling->setValue(j, rightSibling->getValueR(j + 1));
                }
                rightSibling->minusToSize();
                updatePath(rightSibling->getParent(), old, rightSibling->getKey(0));
                updatePath(leaf->getParent(), oldleaf, leaf->getKey(0));
            }
            else if (leftSibling != nullptr) {
                for (size_t j = 0; j < leaf->getSize(); ++j) {
                    leftSibling->setKey(leftSibling->getSize(), leaf->getKey(j));
                    leftSibling->setValue(leftSibling->getSize(), leaf->getValueR(j));
                    leftSibling->addToSize();
                }
                leftSibling->setNextLeaf(leaf->getNextLeaf());
                auto tmp = leaf->getKey(0);
                delete leaf;
                eraseInInternal(leftSibling->getParent(), tmp);
            }
            else {
                for (size_t j = 0; j < rightSibling->getSize(); ++j) {
                    leaf->setKey(leaf->getSize(), rightSibling->getKey(j));
                    leaf->setValue(leaf->getSize(), rightSibling->getValueR(j));
                    leaf->addToSize();
                }
                leaf->setNextLeaf(rightSibling->getNextLeaf());
                auto tmp = rightSibling->getKey(0);
                delete rightSibling;
                eraseInInternal(leaf->getParent(), tmp);
            }
        }
    }

    void eraseInInternal(Internal * internal, const Key & key)
    {
        size_t i = 0;
        while (i < internal->getSize() - 1 && Less{}(internal->getKey(i), key)) {
            ++i;
        }
        for (; i < internal->getSize() - 1; ++i) {
            internal->setKey(i, internal->getKey(i + 1));
            internal->setChildren(i + 1, internal->getChildren(i + 2));
        }
        internal->minusToSize();
        if (internal->getParent() != nullptr && internal->getSize() < (K + 1) / 2) {
            Internal * leftSibling = internal->getLeftSibling(key);
            Internal * rightSibling = internal->getRightSibling(key);
            if (leftSibling != nullptr && leftSibling->getSize() > (K + 1) / 2) {
                Key lastInInternal = internal->getKey(internal->getSize() - 2);
                leftSibling->minusToSize();
                internal->addToSize();
                internal->setChildren(internal->getSize() - 1, internal->getChildren(internal->getSize() - 2));
                for (size_t j = internal->getSize() - 2; j > 0; --j) {
                    internal->setKey(j, internal->getKey(j - 1));
                    internal->setChildren(j, internal->getChildren(j - 1));
                }
                size_t h = 0;
                while (h < leftSibling->getParent()->getSize() - 1 && Less{}(leftSibling->getParent()->getKey(h), leftSibling->getKey(0))) {
                    ++h;
                }
                internal->setKey(0, internal->getParent()->getKey(h - 1));
                internal->setChildren(0, leftSibling->getChildren(leftSibling->getSize()));
                leftSibling->getChildren(leftSibling->getSize())->setParent(static_cast<Node *>(internal));
                internal->getParent()->setKey(h - 1, lastInInternal);
            }
            else if (rightSibling != nullptr && rightSibling->getSize() > (K + 1) / 2) {
                Key firstInRightSibling = rightSibling->getKey(0);
                internal->addToSize();
                size_t h = 0;
                while (h < rightSibling->getParent()->getSize() - 1 && Less{}(rightSibling->getParent()->getKey(h), rightSibling->getKey(0))) {
                    ++h;
                }
                internal->setKey(internal->getSize() - 2, rightSibling->getParent()->getKey(h - 1));
                internal->setChildren(internal->getSize() - 1, rightSibling->getChildren(0));
                rightSibling->getChildren(0)->setParent(static_cast<Node *>(internal));
                rightSibling->minusToSize();
                for (size_t j = 0; j < rightSibling->getSize() - 1; ++j) {
                    rightSibling->setKey(j, rightSibling->getKey(j + 1));
                    rightSibling->setChildren(j, rightSibling->getChildren(j + 1));
                }
                rightSibling->setChildren(rightSibling->getSize() - 1, rightSibling->getChildren(rightSibling->getSize()));
                rightSibling->getParent()->setKey(h - 1, firstInRightSibling);
            }
            else if (leftSibling != nullptr) {
                size_t h = 0;
                while (h < internal->getParent()->getSize() - 1 && Less{}(internal->getParent()->getKey(h), internal->getChildren(0)->getKey(0))) {
                    ++h;
                }
                leftSibling->setKey(leftSibling->getSize() - 1, internal->getParent()->getKey(h));
                leftSibling->setChildren(leftSibling->getSize(), internal->getChildren(0));
                internal->getChildren(0)->setParent(static_cast<Node *>(leftSibling));
                leftSibling->addToSize();
                for (size_t j = 1; j < internal->getSize(); ++j) {
                    leftSibling->setKey(leftSibling->getSize() - 1, internal->getKey(j - 1));
                    leftSibling->setChildren(leftSibling->getSize(), internal->getChildren(j));
                    internal->getChildren(j)->setParent(static_cast<Node *>(leftSibling));

                    leftSibling->addToSize();
                }
                eraseInInternal(internal->getParent(), internal->getParent()->getKey(h - 1));
            }
            else {
                size_t h = 0;
                while (!(h < internal->getParent()->getSize() - 1 && Less{}(internal->getChildren(0)->getKey(0), internal->getParent()->getKey(h)))) { // <=
                    ++h;
                }
                internal->setKey(internal->getSize() - 1, internal->getParent()->getKey(h)); // ТУ ЖЕ ХЕРНЮ ВВЕРХУ
                internal->setChildren(internal->getSize(), rightSibling->getChildren(0));
                rightSibling->getChildren(0)->setParent(static_cast<Node *>(internal)); //
                internal->addToSize();
                for (size_t j = 1; j < rightSibling->getSize(); ++j) {
                    internal->setKey(internal->getSize() - 1, rightSibling->getKey(j - 1));
                    internal->setChildren(internal->getSize(), rightSibling->getChildren(j));
                    rightSibling->getChildren(j)->setParent(static_cast<Node *>(internal)); //
                    internal->addToSize();
                }
                eraseInInternal(internal->getParent(), internal->getParent()->getKey(h - 1));
            }
        }
        if (internal->getParent() == nullptr && internal->getSize() == 1) {
            auto tmp = internal->getChildren(0);
            delete internal;
            root = tmp;
            root->setParent(nullptr);
        }
    }

    iterator erase(const_iterator iteratorToDelete)
    {
        if (upper_bound(iteratorToDelete->first) == end()) {
            erase(iteratorToDelete->first);
            return end();
        }
        Key x = upper_bound(iteratorToDelete->first)->first;
        erase(iteratorToDelete->first);
        return find(x);
    };

    iterator erase(iterator iteratorToDelete)
    {
        if (upper_bound(iteratorToDelete->first) == end()) {
            erase(iteratorToDelete->first);
            return end();
        }
        Key x = upper_bound(iteratorToDelete->first)->first;
        erase(iteratorToDelete->first);
        return find(x);
    };

    iterator erase(const_iterator begin, const_iterator end)
    {
        Key k = end->first;
        iterator it = erase(begin);
        while (Less{}(it->first, k)) {
            it = erase(it);
        }
        return it;
    };

    iterator erase(iterator begin, iterator end)
    {
        Key k = end->first;
        iterator it = erase(begin);
        while (Less{}(it->first, k)) {
            it = erase(it);
        }
        return it;
    };

    size_type erase(const Key & key)
    {
        if (contains(key)) {
            treeSize--;
            eraseInLeaf(findLeaf(key), key);
            return 1;
        }
        else {
            return 0;
        }
    };

    ~BPTree()
    {
        clear();
    }
};