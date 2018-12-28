#!/bin/bash

export export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.
java -agentlib:NativeDecrypt=dec_ids_server.xml -jar snort_parser_enc.jar
