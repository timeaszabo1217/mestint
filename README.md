# Mesterséges intelligencia 1

A projekt célja egy önműködő kígyó AI vezérlő (Agent) megalkotása volt, amely képes optimalizált döntéseket hozni a játéktérben különböző algoritmusok alkalmazásával.

## Feladat szöveges leírása

A feladat során egy `Agent.java` osztályt kellett létrehozni, amely egy önállóan működő mesterséges intelligenciát (AI-t) valósít meg a klasszikus **Snake (kígyó)** játékban. Az AI célja, hogy a játékot legalább 3000 pontig játssza le automatikusan, **10 különböző véletlenszerű seed** mellett.

## Követelmények

### Algoritmusok

Az AI különféle algoritmusokat kombinál a legoptimálisabb döntések meghozatalára, hogy minél tovább éljen és több pontot gyűjtsön:

- **A\* algoritmus**:  
  - Elsődleges útvonal-keresés az étel irányába.  
  - A legrövidebb biztonságos útvonal meghatározására használatos, amennyiben elérhető.

- **BFS (Breadth-First Search)**:  
  - Tartalék stratégia, ha az A\* nem talál biztonságos utat.  
  - Egyszerű, de robusztus megközelítés az elérhető terek feltérképezéséhez.

- **Hamilton-kör megközelítés**:  
  - Alkalmazásra kerül, ha sem A\*, sem BFS nem garantál biztonságos ételhez jutást.  
  - A kígyó úgy mozog, hogy a teljes pályát bejárja, elkerülve az öngyilkosságot, így végtelen élet biztosítható elméleti szinten.

### AI működése

- A `Agent.java` osztályban kerül implementálásra a döntéshozatal.  
- A kígyó minden lépésnél elemzi a pályát, és választ a fenti algoritmusok közül.
- A program legalább 10 különböző futtatás során stabilan képes 3000+ pont elérésére.

### Vizualizáció

- A projekt során tesztelés céljából grafikus felület vagy konzolos kimenet használható a kígyó mozgásának követésére.
- A játék eredményei és pontszámai naplózhatók a későbbi elemzéshez vagy értékeléshez.

### Biztonság és hatékonyság

- Az AI által használt algoritmusok hatékonyak, és ciklikusan elemzik a játéktér aktuális állapotát.
- A Hamilton-körös stratégia megelőzi a zsákutcába futást, amikor nincs biztonságos út az ételhez.
- Minden döntés előtt az AI validálja a következő lépést, hogy elkerülje az ütközést a falakkal vagy a saját testével.
