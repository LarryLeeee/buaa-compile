#!/bin/bash
echo "" >error.txt
a=1
b=0
while [ $a -ne 7 ]; do
  mv testfile$a.c testfile$a.txt
  a=$(($a + 1))
done