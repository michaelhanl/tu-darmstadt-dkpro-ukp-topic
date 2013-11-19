#!/bin/bash

## set runtime parameters 
## in "all" mode the rule file is automatically detected, if there are rule files in the "config"
## directory with the respective language extension ("EN" or "DE").
v3="dkpro.ukp.topic.structure.jar"
v4="dkpro.ukp.topic.structure-core-1.0-jar-with-dependencies.jar"
input="input"
output="output"
model="pcfg"

parameters="-all $1 -model $model -inDir $input -outDir $output"

java -jar $v4 $parameters


## java -cp annolab.jar org.annolab.tre.cli.Main Rules_EN.xml file.xml
## java -cp annolab.jar org.annolab.tre.cli.LearnMain Rules_EN.xml file.xml out.xml
