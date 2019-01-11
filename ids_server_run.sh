#!/bin/bash

export LD_LIBRARY_PATH=.:jre/bin:$LD_LIBRARY_PATH
java -agentlib:NativeDecrypt=dec_ids_server.xml -jar snort_parser_enc.jar
