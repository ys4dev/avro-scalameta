package com.example.avro

import annotation.MyAvroRecord

/**
  *
  */
@MyAvroRecord("/example.avsc", "com.example.avro.EmailAddress")
case class ToDoItem() {

}
