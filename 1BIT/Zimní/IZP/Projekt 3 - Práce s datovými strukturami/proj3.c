/**
 * Kostra programu pro 3. projekt IZP 2018/19
 *
 * Jednoducha shlukova analyza: 2D nejblizsi soused.
 * Single linkage
 */
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <math.h> // sqrtf
#include <limits.h> // INT_MAX

/*****************************************************************
 * Ladici makra. Vypnout jejich efekt lze definici makra
 * NDEBUG, napr.:
 *   a) pri prekladu argumentem prekladaci -DNDEBUG
 *   b) v souboru (na radek pred #include <assert.h>
 *      #define NDEBUG
 */
#ifdef INFINITY
//Nekonecno
#endif
#ifdef NDEBUG
#define debug(s)
#define dfmt(s, ...)
#define dint(i)
#define dfloat(f)
#else

// vypise ladici retezec
#define debug(s) printf("- %s\n", s)

// vypise formatovany ladici vystup - pouziti podobne jako printf
#define dfmt(s, ...) printf(" - "__FILE__":%u: "s"\n",__LINE__,__VA_ARGS__)

// vypise ladici informaci o promenne - pouziti dint(identifikator_promenne)
#define dint(i) printf(" - " __FILE__ ":%u: " #i " = %d\n", __LINE__, i)

// vypise ladici informaci o promenne typu float - pouziti
// dfloat(identifikator_promenne)
#define dfloat(f) printf(" - " __FILE__ ":%u: " #f " = %g\n", __LINE__, f)

#endif

/*****************************************************************
 * Deklarace potrebnych datovych typu:
 *
 * TYTO DEKLARACE NEMENTE
 *
 *   struct obj_t - struktura objektu: identifikator a souradnice
 *   struct cluster_t - shluk objektu:
 *      pocet objektu ve shluku,
 *      kapacita shluku (pocet objektu, pro ktere je rezervovano
 *          misto v poli),
 *      ukazatel na pole shluku.
 */

struct obj_t {
    int id;
    float x;
    float y;
};

struct cluster_t {
    int size;
    int capacity;
    struct obj_t *obj;
};

/*****************************************************************
 * Deklarace potrebnych funkci.
 *
 * PROTOTYPY FUNKCI NEMENTE
 *
 * IMPLEMENTUJTE POUZE FUNKCE NA MISTECH OZNACENYCH 'TODO'
 *
 */

/*
 Inicializace shluku 'c'. Alokuje pamet pro cap objektu (kapacitu).
 Ukazatel NULL u pole objektu znamena kapacitu 0.
*/
void init_cluster(struct cluster_t *c, int cap)
{
    assert(c != NULL);
    assert(cap >= 0);
    c->size = 0;
    if((cap > 0)&&(c->obj = malloc(sizeof(struct obj_t) * cap)))
    {
        c->capacity = cap;
        return;
    }
    c->obj = NULL;
    c->capacity = 0;
}

/*
 Odstraneni vsech objektu shluku a inicializace na prazdny shluk.
 */
void clear_cluster(struct cluster_t *c)
{
    // TODO
    free(c->obj);
    init_cluster(c,0);
}

/// Chunk of cluster objects. Value recommended for reallocation.
const int CLUSTER_CHUNK = 10;

/*
 Zmena kapacity shluku 'c' na kapacitu 'new_cap'.
 */
struct cluster_t *resize_cluster(struct cluster_t *c, int new_cap)
{
    // TUTO FUNKCI NEMENTE
    assert(c);
    assert(c->capacity >= 0);
    assert(new_cap >= 0);

    if (c->capacity >= new_cap)
        return c;

    size_t size = sizeof(struct obj_t) * new_cap;

    void *arr = realloc(c->obj, size);
    if (arr == NULL)
        return NULL;

    c->obj = (struct obj_t*)arr;
    c->capacity = new_cap;
    return c;
}

/*
 Prida objekt 'obj' na konec shluku 'c'. Rozsiri shluk, pokud se do nej objekt
 nevejde.
 */
void append_cluster(struct cluster_t *c, struct obj_t obj)
{
    //printf("Chyba na radku\n");

    if(c->capacity <= c->size)
        if(!resize_cluster(c, c->capacity + CLUSTER_CHUNK)) return;
    
    c->obj[c->size++] = obj;
}

/*
 Seradi objekty ve shluku 'c' vzestupne podle jejich identifikacniho cisla.
 */
void sort_cluster(struct cluster_t *c);

/*
 Do shluku 'c1' prida objekty 'c2'. Shluk 'c1' bude v pripade nutnosti rozsiren.
 Objekty ve shluku 'c1' budou serazeny vzestupne podle identifikacniho cisla.
 Shluk 'c2' bude nezmenen.
 */
void merge_clusters(struct cluster_t *c1, struct cluster_t *c2)
{
    assert(c1 != NULL);
    assert(c2 != NULL);
    for(int i = 0; i < c2->size; i++)
    {
        append_cluster(c1,c2->obj[i]);
    }
    sort_cluster(c1);
}

/**********************************************************************/
/* Prace s polem shluku */

/*
 Odstrani shluk z pole shluku 'carr'. Pole shluku obsahuje 'narr' polozek
 (shluku). Shluk pro odstraneni se nachazi na indexu 'idx'. Funkce vraci novy
 pocet shluku v poli.
*/
int remove_cluster(struct cluster_t *carr, int narr, int idx)
{
    assert(idx < narr);
    assert(narr > 0);
    clear_cluster(&carr[idx]);

    for(int i = idx; i < narr-1; i++)
    {
        carr[i] = carr[i + 1];
    }
    return narr - 1;

    // TODO
}

/*
 Pocita Euklidovskou vzdalenost mezi dvema objekty.
 */
float obj_distance(struct obj_t *o1, struct obj_t *o2)
{
    assert(o1 != NULL);
    assert(o2 != NULL);
    return sqrt((o2->x - o1->x)*(o2->x - o1->x) + (o2->y-o1->y)*(o2->y-o1->y));
}

/*
 Pocita vzdalenost dvou shluku.
*/
float cluster_distance(struct cluster_t *c1, struct cluster_t *c2)
{
    assert(c1 != NULL);
    assert(c1->size > 0);
    assert(c2 != NULL);
    assert(c2->size > 0);

    float distnace = 0;
    float newDistance = INFINITY;

    for(int i = 0; i < c1->size; i++)
        for(int j = i; j < c2->size; j++)
        {
            distnace = obj_distance(&c1->obj[i], &c2->obj[j]);
            if(distnace < newDistance) 
            {
                newDistance = distnace;
                //printf("%lf\n", newDistance);
            }
        }
    return newDistance;

    // TODO
}

/*
 Funkce najde dva nejblizsi shluky. V poli shluku 'carr' o velikosti 'narr'
 hleda dva nejblizsi shluky. Nalezene shluky identifikuje jejich indexy v poli
 'carr'. Funkce nalezene shluky (indexy do pole 'carr') uklada do pameti na
 adresu 'c1' resp. 'c2'.
*/
void find_neighbours(struct cluster_t *carr, int narr, int *c1, int *c2)
{
    float smallestDistance = INFINITY;
    float distance = 0;
    assert(narr > 0);
    for(int i = 0; i < narr; i++)
        for(int j = i + 1; j < narr; j++)
        {
            distance = cluster_distance(&carr[i],&carr[j]);
            if(smallestDistance > distance)
            {
                smallestDistance = distance;
                *c1 = i;
                *c2 = j;
            }
        }
    // TODO
}

// pomocna funkce pro razeni shluku
static int obj_sort_compar(const void *a, const void *b)
{
    // TUTO FUNKCI NEMENTE
    const struct obj_t *o1 = (const struct obj_t *)a;
    const struct obj_t *o2 = (const struct obj_t *)b;
    if (o1->id < o2->id) return -1;
    if (o1->id > o2->id) return 1;
    return 0;
}

/*
 Razeni objektu ve shluku vzestupne podle jejich identifikatoru.
*/
void sort_cluster(struct cluster_t *c)
{
    // TUTO FUNKCI NEMENTE
    qsort(c->obj, c->size, sizeof(struct obj_t), &obj_sort_compar);
}

/*
 Tisk shluku 'c' na stdout.
*/
void print_cluster(struct cluster_t *c)
{
    // TUTO FUNKCI NEMENTE
    for (int i = 0; i < c->size; i++)
    {
        if (i) putchar(' ');
        printf("%d[%g,%g]", c->obj[i].id, c->obj[i].x, c->obj[i].y);
    }
    putchar('\n');
}
/*
Vlastni funkce
*/
void init_clusters(struct cluster_t **clus, int cap)
{
    if(! (*clus = malloc(sizeof(struct cluster_t) * cap)))
        return;
    for(int i = 0; i < cap; i++) 
                init_cluster(&(*clus)[i],0);
}
void clear_all_clusters(struct cluster_t *clus, int cap)
{
    for(int i = 0; i < cap; i++)
        clear_cluster(&clus[i]);
    free(clus);
}
int change_size_of_clusters(struct cluster_t **clus,int size, int newSize)
{
    if(newSize > size)
    {
        printf("Pocet shluku nesmi byt veci nez pocet objektu!\n");
        return -1;
    }
    int c1,c2;
    while(newSize < size)
    {
        find_neighbours(*clus,size,&c1,&c2);
        merge_clusters(&(*clus)[c1],&(*clus)[c2]);
        //clear_cluster(&(*clus)[c2]);
        size = remove_cluster(*clus,size,c2);
    }
    return size;
}
/*
 Ze souboru 'filename' nacte objekty. Pro kazdy objekt vytvori shluk a ulozi
 jej do pole shluku. Alokuje prostor pro pole vsech shluku a ukazatel na prvni
 polozku pole (ukalazatel na prvni shluk v alokovanem poli) ulozi do pameti,
 kam se odkazuje parametr 'arr'. Funkce vraci pocet nactenych objektu (shluku).
 V pripade nejake chyby uklada do pameti, kam se odkazuje 'arr', hodnotu NULL.
*/
int load_clusters(char *filename, struct cluster_t **arr)
{
    assert(arr != NULL);

    FILE * fp;
    int maxLineLen = 256;
    char line[maxLineLen-1];
    fp = fopen(filename, "r");
    int lineNumber = 0,objX,objY,objID;
    int totalObjectCount = 0;
    struct obj_t obj;
    struct cluster_t * cluster;
    char lastCh;

    if(fp == NULL)
        return -2;
    else
    {
        while(fgets(line, maxLineLen, fp) != NULL)
        {
            ++lineNumber;
            if(lineNumber == 1)
            {
                if(sscanf(line,"count=%d%[^\n]",&totalObjectCount,&lastCh) != 1)
                {
                    printf("Chyba na radku %d\n", lineNumber);
                    fclose(fp);
                    return -2;
                }
                if(totalObjectCount <= 0)
                {
                    printf("count=%d! Musi byt veci nez 0\n",totalObjectCount);
                    fclose(fp);
                    return -2;
                }
                init_clusters(arr, totalObjectCount);
                if(*arr == NULL)
                {
                    printf("Chyba pri alokaci\n");
                    fclose(fp);
                    return -1;
                }
                continue;
            }
            if(lineNumber > totalObjectCount+1) break;

            if(sscanf(line,"%d %d %d%[^\n]",&objID,&objX,&objY,&lastCh) != 3 || objX < 0 || objX > 1000 || objY < 0 || objY > 1000)
            {
                printf("Chyba na radku %d\n", lineNumber);
                clear_all_clusters(*arr, totalObjectCount);
                *arr = NULL;
                fclose(fp);
                return -1;
            }
            obj.id = objID;
            obj.x = objX;
            obj.y = objY;

            cluster = &(*arr)[lineNumber - 2];

            append_cluster(cluster, obj);
            if(cluster->size != 1)
            {
                printf("Chyba pri alokaci pameti\n");
                clear_all_clusters(*arr, totalObjectCount);
                *arr = NULL;
                fclose(fp);
                return -1;
            }
        }
        fclose(fp);
        if(lineNumber - 1 < totalObjectCount)
        {
            printf("Spaty pocet objektu\n");
            clear_all_clusters(*arr, totalObjectCount);
            *arr = NULL;
            return -1;
        }
    }
    return totalObjectCount;
}

/*
 Tisk pole shluku. Parametr 'carr' je ukazatel na prvni polozku (shluk).
 Tiskne se prvnich 'narr' shluku.
*/
void print_clusters(struct cluster_t *carr, int narr)
{
    printf("Clusters:\n");
    for (int i = 0; i < narr; i++)
    {
        printf("cluster %d: ", i);
        print_cluster(&carr[i]);
    }
}

int main(int argc, char *argv[])
{
    int clusterCount;
    char lastCh;
    if(argc == 3)
    {
        if((sscanf(argv[2],"%d%[^\n]",&clusterCount,&lastCh) != 1) || (clusterCount <= 0))
            return -1;
    }
    else if(argc == 2)
    {
        clusterCount = 1;
    }
    else
    {
        return -1;
    }
        struct cluster_t *clusters;
        int objectCount = load_clusters(argv[1],&clusters);
        if(objectCount == -1)
        {
            //clear_all_clusters(clusters,objectCount);
            return -1;
        }
        else if(objectCount == -2)
        {
            return -1;
        }

        if(change_size_of_clusters(&clusters,objectCount, clusterCount) == -1)
        {
            clear_all_clusters(clusters,objectCount);
            return -1;
        }
        print_clusters(clusters, clusterCount);
        clear_all_clusters(clusters,clusterCount);
        return 0;
}