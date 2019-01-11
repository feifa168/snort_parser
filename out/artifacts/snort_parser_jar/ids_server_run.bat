@echo off

set path=.;jre\bin;%path%

java -agentlib:libNativeDecrypt=dec_ids_server.xml -jar snort_parser_enc.jar
