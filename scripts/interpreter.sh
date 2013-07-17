#!/bin/bash

cd ..

v1="dkproTopic.jar"
v2="themeOffice.jar"
v3="all_intellij.jar"
input="output"
output="$input/topics"
rule="config/Rules_EN.xml"

parameters="-topic -rules $rule -inDir $input -outDir $output -toFile"

#java -jar themeOffice.jar $parameters

java -jar $v3 $parameters


## java -cp annolab.jar org.annolab.tre.cli.Main Rules_EN.xml file.xml
## java -cp annolab.jar org.annolab.tre.cli.LearnMain Rules_EN.xml file.xml out.xml

#java -jar all_intellij.jar -topic -rules config/Rules_EN.xml -inDir output -outDir output/topics -toFile