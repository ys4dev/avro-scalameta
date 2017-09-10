package com.example.avro

import annotation.MyAvroRecord

@MyAvroRecord("/example.avsc", "com.example.avro.User")
case class User()
