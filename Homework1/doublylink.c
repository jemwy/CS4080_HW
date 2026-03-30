#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

typedef struct Node {
    char *value;
    struct Node *prev;
    struct Node *next;
} Node;

typedef struct {
    Node *head;
    Node *tail;
    size_t size;
} DList;

static void *xmalloc(size_t n) {
    void *p = malloc(n);
    if (!p) {
        fprintf(stderr, "Out of memory\n");
        exit(1);
    }
    return p;
}

static char *xstrdup(const char *s) {
    size_t len = strlen(s) + 1;
    char *copy = (char *)xmalloc(len);
    memcpy(copy, s, len);
    return copy;
}

static void dlist_init(DList *list) {
    list->head = NULL;
    list->tail = NULL;
    list->size = 0;
}

static Node *node_create(const char *s) {
    Node *n = (Node *)xmalloc(sizeof(Node));
    n->value = xstrdup(s);
    n->prev = NULL;
    n->next = NULL;
    return n;
}

static void dlist_insert(DList *list, const char *s) {
    Node *n = node_create(s);

    if (list->tail == NULL) {
        list->head = n;
        list->tail = n;
    } else {
        n->prev = list->tail;
        list->tail->next = n;
        list->tail = n;
    }
    list->size++;
}

static Node *dlist_find(const DList *list, const char *s) {
    for (Node *cur = list->head; cur != NULL; cur = cur->next) {
        if (strcmp(cur->value, s) == 0) {
            return cur;
        }
    }
    return NULL;
}

static void dlist_delete_node(DList *list, Node *n) {
    assert(n != NULL);

    if (n->prev) {
        n->prev->next = n->next;
    } else {
        list->head = n->next;
    }

    if (n->next) {
        n->next->prev = n->prev;
    } else {
        list->tail = n->prev;
    }

    free(n->value);
    free(n);
    list->size--;
}

static int dlist_delete(DList *list, const char *s) {
    Node *n = dlist_find(list, s);
    if (!n) return 0;
    dlist_delete_node(list, n);
    return 1;
}

static void dlist_destroy(DList *list) {
    Node *cur = list->head;
    while (cur) {
        Node *next = cur->next;
        free(cur->value);
        free(cur);
        cur = next;
    }
    list->head = list->tail = NULL;
    list->size = 0;
}

static void dlist_print(const DList *list) {
    printf("[");
    for (Node *cur = list->head; cur != NULL; cur = cur->next) {
        printf("\"%s\"", cur->value);
        if (cur->next) printf(", ");
    }
    printf("] (size=%zu)\n", list->size);
}

int main(void) {
    DList list;
    dlist_init(&list);

    dlist_insert(&list, "alpha");
    dlist_insert(&list, "beta");
    dlist_insert(&list, "gamma");
    dlist_insert(&list, "beta");
    assert(list.size == 4);
    dlist_print(&list);

    Node *f1 = dlist_find(&list, "beta");
    assert(f1 != NULL && strcmp(f1->value, "beta") == 0);
    Node *f2 = dlist_find(&list, "delta");
    assert(f2 == NULL);

    assert(dlist_delete(&list, "beta") == 1);
    assert(list.size == 3);
    dlist_print(&list);

    assert(dlist_delete(&list, "beta") == 1);
    assert(list.size == 2);
    dlist_print(&list);

    assert(dlist_delete(&list, "beta") == 0);
    assert(list.size == 2);

    assert(dlist_delete(&list, "alpha") == 1);
    assert(list.size == 1);
    dlist_print(&list);

    assert(dlist_delete(&list, "gamma") == 1);
    assert(list.size == 0);
    dlist_print(&list);

    dlist_destroy(&list);
    printf("All tests passed.\n");
    return 0;
}