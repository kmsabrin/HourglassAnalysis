#!/bin/bash
for ((i = 1; i < 40; i++))
do
	gcc  "openssh-$i.c" -o "openssh-$i.o" -lssl -lcrypt -lresolv -lutil -lpam
	genfull -g cobjdump -o "full.graph-openssh-$i"
	rm -f "openssh-$i.o"
done
