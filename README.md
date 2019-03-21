[![Build Status](https://api.travis-ci.org/soabase/asm-mirror-descriptor.svg?branch=master)](https://travis-ci.org/soabase/asm-mirror-descriptor)
[![Maven Central](https://img.shields.io/maven-central/v/io.soabase.asm-mirror-descriptor/asm-mirror-descriptor-core.svg)](http://search.maven.org/#search%7Cga%7C1%7Casm-mirror-descriptor)

# ASM Mirror Descriptor
Addition to [OW2 ASM library](https://asm.ow2.io) to support generating descriptors and signatures from TypeMirrors/Elements.

## Background
The ASM library has classes that can process Java class files through a Visitor style API allowing users to examine or modify 
any part of a class. The ASM Mirror project adds comparable classes to do the same with [TypeMirror](https://docs.oracle.com/javase/8/docs/api/javax/lang/model/type/TypeMirror.html)
and [Element](https://docs.oracle.com/javase/8/docs/api/javax/lang/model/element/Element.html) instances used during [Annotation
Processing](https://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html).

## Corresponding Classes

| ASM Class | ASM Mirror Class | Description |
| --------- | ---------------- | ----------- |
| ClassReader | MirrorClassReader | A parser to make a ClassVisitor visit a TypeMirror/Element instance. |
| Type | MirrorType | A Java field or method type. |

## Additional Classes

The Java spec requires "signatures" for generic descriptors. The ASM library has some internal utilities for generating these.
The ASM mirror library provides `SignatureMirrorType` to generate generic signatures from TypeMirrors/Elements.


