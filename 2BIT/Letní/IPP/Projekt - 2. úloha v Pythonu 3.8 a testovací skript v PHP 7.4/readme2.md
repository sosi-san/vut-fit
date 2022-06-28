Implementační dokumentace k 2. úloze do IPP 2020/2021
Jméno a příjmení: Adam Woska
Login: xwoska00


### Cíl projektu
Cílem druhé části projektu bylo vytvořit skript interpret\.py, který dostane na **STDIN** XML reprezentaci jazyka IPPcode21 a provede jeho interpretaci.
Dále bylo za úkol vytvořit testovací skript *test.php* který umožnuje automatické testovaní.

### interpret.py
Na začátku skript vyhodnotí argumenty. Při argumentu **-\-help**. skript vypíše zadanou hodnotu a ukončí se.
Z argumentu **-\-source**. získá vstupní soubor s IPPcode21. A převede ho na XML strom.
Poté se kontroluje, jestli struktura stromu odpovídá zadání.
Z data ze stromu se převedou na instrukce reprezentované třídou Instruction a argumenty reprezentované třídou Arg.
V poli instrukcí pak vyhledám všechny lablels a uložím je do slovníku. Kontroluji, jestli nějaké label není 2x.
Poté se přechází na samotnou interpretaci instrukcí.
Při které se kontroluje, jestli má instrukce správné operandy a pokud ano tak se instrukce interpretuje.

### test.php
Na začátku skript vyhodnotí argumenty. Při argumentu **-\-help**. skript vypíše zadanou hodnotu a ukončí se.
Poté procházím zadaný adresář a získávám všechny soubory co končí příponou .src a ukládám je do třídy TestItem.
Nad kterými následně provdáním samotné testování a výsledky testování opět ukládám do třídy TestItem.
Jakmile je testování dokončeno tak se generuje html z údajů uložených v poli TestItemů.