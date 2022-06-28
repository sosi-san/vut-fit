/**
 * @mainpage Projekt 3
 * @link
 * proj3.h
 * @endlink
 *
 * @file proj3.h
 * @brief Projekt 3 - Jednoducha shlukova analyza
 * @date Prosinec 2018
 *
 */

/**
 * @brief Struktura představující objekt se souřadnicemi a identifikátorem
 */
struct obj_t {
    /** identifikator */
    int id;
    /** souradnice x */
    float x;
    /** souradnice y */
    float y;
};
/**
 * @brief Struktura představující shluk objektu
 */
struct cluster_t {
    /** pocet objektu ve shluku */
    int size;
    /** kapacita shluku (kolik je místa pro objekty) */
    int capacity;
    /** pole objketů shluku */
    struct obj_t *obj;
};


/**
 * @defgroup Prace se shluky
 * @{
 */

/**
 * Inicializace shluku 'c'. Alokuje pamet pro cap objektu (kapacitu).
 * Ukazatel NULL u pole objektu znamena kapacitu 0.
 *
 * @post
 * Pokud nenastane chyba tak se shluku 'c' alokuje pamet pro 'cap' objektu.
 * 
 * @param c shluk ktery ma byt inicializovan
 * @param cap pozadovana kapacita shluku
 */
void init_cluster(struct cluster_t *c, int cap);

/**
 * Odstraneni vsech objektu shluku a inicializace na prazdny shluk.
 *
 * @post
 * Dealokace (uvolneni) pameti pro vsehny shluky 'c'.
 * 
 * @param c shluk ktery ma byt odstranen
 */
void clear_cluster(struct cluster_t *c);

/// Chunk of cluster objects. Value recommended for reallocation.
extern const int CLUSTER_CHUNK;

/**
 * Zmena kapacity shluku 'c' na kapacitu 'new_cap'.
 *
 * @post
 * Pokud nenastane chyba pri alokaci tak bude kapacita shluku 'c' zmeněna na kapacitu 'new_cap'.
 *
 * @param c shluk pro změnu
 * @param new_cap nová cílová kapacita shluku
 * @return shluk s novou kapacitou nebo NULL v případě chyby
 *
 *
 */
struct cluster_t *resize_cluster(struct cluster_t *c, int new_cap);
/**
 * Prida objekt 'obj' na konec shluku 'c'. Rozsiri shluk, pokud se do nej objekt
 * nevejde.
 *
 * @post
 * Objekt 'obj' se bude nacházet na poslední pozici shluku 'c' pokud nenastane chyba
 *
 * @param c shluk pro pridani objektu
 * @param obj objekt k přidání do shluku
 */
void append_cluster(struct cluster_t *c, struct obj_t obj);

/**
 * Seradi objekty ve shluku 'c' vzestupne podle jejich identifikacniho cisla.
 *
 * @post
 * objekty budou seřazeny podle ID a to vzestupně
 *
 * @param c shluk s objekty pro seřazení
 *
 */
void sort_cluster(struct cluster_t *c);

/**
 * Do shluku 'c1' prida objekty 'c2'. Shluk 'c1' bude v pripade nutnosti rozsiren.
 * Objekty ve shluku 'c1' budou serazeny vzestupne podle identifikacniho cisla.
 * Shluk 'c2' bude nezmenen.
 *
 * @post
 * Do shluku 'c1' s přidají objekty z hluku 'c2', pokud nenastane chyba
 *
 * @param c1 shluk do kterého budou přidány objekty
 * @param c2 shluk z kterého budou přidávány objekty
 */
void merge_clusters(struct cluster_t *c1, struct cluster_t *c2);

/**********************************************************************/
/* Prace s polem shluku */

/**
 * Odstrani shluk z pole shluku 'carr'. Pole shluku obsahuje 'narr' polozek
 * (shluku). Shluk pro odstraneni se nachazi na indexu 'idx'. Funkce vraci novy
 * pocet shluku v poli.
 * 
 * @post
 * Z pole shluků 'carr' bude odstraněn shluk na pozici 'idx' pole shluků bude o jedno menší
 *
 *
 * @param carr pole shluků ze ktereho budeme odstraňovat shluk
 * @param narr velikost pole (pocet prvku)
 * @param idx index shluku k odstaneni
 * @return nova velikost pole (pocet prvku)
 *
 */
int remove_cluster(struct cluster_t *carr, int narr, int idx);
/**
 * @}
 */

/**
 * @defgroup Prace s polem
 * @{
 */

/**
 * Pocita Euklidovskou vzdalenost mezi dvema objekty.
 *
 * @see https://en.wikipedia.org/wiki/Euclidean_distance
 *
 * @param o1 1. objekt
 * @param o2 2. objekt
 * @return Euklidovska vzálenost mezi dvěma objekty
 */

float obj_distance(struct obj_t *o1, struct obj_t *o2);

/**
 * Pocita vzdalenost dvou shluku.
 *
 * @param c1 1. shluk
 * @param c2 2. shluk
 * @return vzdálenost dvou shluků
 */
float cluster_distance(struct cluster_t *c1, struct cluster_t *c2);

/**
 * Funkce najde dva nejblizsi shluky. V poli shluku 'carr' o velikosti 'narr'
 * hleda dva nejblizsi shluky. Nalezene shluky identifikuje jejich indexy v poli
 * 'carr'. Funkce nalezene shluky (indexy do pole 'carr') uklada do pameti na
 * adresu 'c1' resp. 'c2'.
 *
 * @post
 * Najde indexi dvou nejbližích shluků ty uloži do c1 a c2.
 *
 * @param carr pole shluku
 * @param narr pocet prvku pole
 * @param c1 index jednoho shluku
 * @param c2 index druheho shluku
 */
void find_neighbours(struct cluster_t *carr, int narr, int *c1, int *c2);

/**
 * Tisk shluku 'c' na stdout.
 * 
 * @post
 * Objekty ze zhluku budou vypsán na stdout
 *
 * @param c shluk pro výtisk
 */
void print_cluster(struct cluster_t *c);

/**
 * Ze souboru 'filename' nacte objekty. Pro kazdy objekt vytvori shluk a ulozi
 * jej do pole shluku. Alokuje prostor pro pole vsech shluku a ukazatel na prvni
 * polozku pole (ukalazatel na prvni shluk v alokovanem poli) ulozi do pameti,
 * kam se odkazuje parametr 'arr'. Funkce vraci pocet nactenych objektu (shluku).
 * V pripade nejake chyby uklada do pameti, kam se odkazuje 'arr', hodnotu NULL.
 *
 * @post
 * Pro každá objekt v souboru bude vytvořen jeden shluk tyto shluky se uloží do pole shluké 'arr'
 * pro které se alokuje pamět
 *
 * @param filename název souboru s objekty
 * @param arr ukaztatel na pole shluků
 * @return pocet nactených objektů (při chybě -1)
 */
int load_clusters(char *filename, struct cluster_t **arr);

/**
 * Tisk pole shluku. Parametr 'carr' je ukazatel na prvni polozku (shluk).
 * Tiskne se prvnich 'narr' shluku.
 *
 * @post
 * Každý shluk z pole zhuků bude vypsán na stdout
 *
 * @param carr pole pro tisk
 * @param narr pocet shluku v poli
 */
void print_clusters(struct cluster_t *carr, int narr);

/**
 * @}
 */
