[![Java CI with Maven](https://github.com/starvy/duchchess/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/starvy/duchchess/actions/workflows/maven.yml)
![](https://img.shields.io/tokei/lines/github/starvy/duchchess)
![](https://img.shields.io/github/license/starvy/duchchess)
![](https://img.shields.io/github/v/release/starvy/duchchess)
# Duch Chess Engine

Duch is a UCI Chess Engine written in Java. Version 1.0.0 is ranked around 2000 ELO

## :computer_mouse: Play it

In order to play with Duch, you need a chess GUI that supports UCI protocol like **cute-chess** or **Arena**

## Motivation

My goal was to learn about chess programming and create an engine that beats me in chess. Now, it beats me every time.

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
mvn package
```

## :technologist: Author

- Sebastian Pravda

## Contributing
Any contribution or help is welcomed.

### Please note
This is just a hobby project. There might be some bugs in the code.
