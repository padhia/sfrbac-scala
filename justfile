alias r := run
alias b := build
alias t := test
alias u := update

_help:
  @just --list

test:
  mill test

run *args:
  mill run {{args}}

update:
  mill mill.scalalib.Dependency/showUpdates

clean:
  rm -rf out .metals .bloop

rr:
  mill run ./sample.yaml | bat --wrap=never -l sql

build:
  rm -rf out/*
  mill publishLocal
  cs bootstrap org.padhia::sfenv:0.1.0 -M sfenv.Main -f -o ~/.local/bin/sfenv
