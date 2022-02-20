# Duch Chess Engine

Duch is a UCI Chess Engine written in Java. Version 0.1.0 is ranked around 2000 ELO

## :computer_mouse: Play it

In order to play with Duch, you need a chess GUI that supports UCI protocol like **cute-chess** or **Arena**

## :mag_right: Features

- Bitboards
- Magic Bitboards
- Transposition Table
- Zobrist Key Hasing
- Static Exchange Evaluation
- Negamax Search
  - Alpha-beta pruning
  - Late move pruning
  - Late move reductions
  - Razoring pruning
  - Futility pruning
  - Iterative deepening
- Move Ordering
  - Hash Move
  - Most valuable victim / Least valuable attacker
  - Killer moves
  - History Heuristics
- Evaluation
  - Material value
  - Material position
  - Piece mobility
  - Pawn evaluation

## :pick: Building

Duch is compiled using Maven

```shell
mvn package duch
```

## :technologist: Author

- Sebastian Pravda
