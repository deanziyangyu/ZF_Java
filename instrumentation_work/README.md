# ZF_Java
A Java implementation of ZombieFuzz to produce fuzzers for Java programs.

## Setup and Compile

Clone this repo by running
`git clone https://github.com/deanziyangyu/ZF_Java.git`
or by a method of your choice.

Download the soot package by running
`./scripts/download-soot`
in the `ZF_Java` directory.

The file will be download to the following directory:
`path-to-ZF_Java/downloads/soot/build/`

Add the following lines to your ~/.bashrc script:
`export CLASSPATH=path-to-ZF_Java/downloads/soot/build/sootclasses-trunk-jar-with-dependencies.jar:$CLASSPATH`
`export CLASSPATH=path-to-ZF_Java/bin:$CLASSPATH`
Source your bashrc or enter a new terminal.

Now, compile the `ZF_Java` source code by running
`./scripts/build-package`

Finally, test that soot is working by running the following script:
`./scripts/hello-world-test`

