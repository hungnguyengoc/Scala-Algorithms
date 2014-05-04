/**
 * @see https://github.com/jsuereth/scala-in-depth-source/blob/master/chapter11/src/main/scala/scalax/resource/resource.scala
 */
package org.gs.fixtures

import scala.io.Source
import scala.collection.immutable.TreeMap
import org.gs.digraph.Digraph
import scala.io.BufferedSource
import scala.collection.mutable.ArrayBuffer

/**
 * @author Gary Struthers
 *
 */
trait ManagedResource[T] {
  def loan[U](f: T => U): U
}

trait BufferedSourceBuilder {
  def readURI(uri: String) = new ManagedResource[BufferedSource] {
    /**
     * BufferedSource can only be iterated once
     * @param f
     * @return
     */
    def loan[U](f: BufferedSource => U): U = {
      val bufferdSource = Source.fromURL(uri)
      try {
        f(bufferdSource)
      } finally {
        bufferdSource.close
      }
    }
  }
}

trait StringArrayBuilder extends BufferedSourceBuilder {
  
  def readFileToArray(buffSource: BufferedSource): ArrayBuffer[String] = {
    val savedLines = new ArrayBuffer[String]()
    val it = buffSource.getLines
    for (a <- it) savedLines.append(a)
    savedLines
  }

  def buildFromManagedResource(uri: String): ArrayBuffer[String] = {
    val managedResource = readURI(uri)
    managedResource.loan(readFileToArray)
  }
}

trait SymbolTableBuilder {

  def buildStringIndex(delimiter: String, savedLines: ArrayBuffer[String]): TreeMap[String, Int] = {
    var st = new TreeMap[String, Int]()
    for {
      a <- savedLines
      s <- a.split(delimiter)
      if (!st.contains(s))
    } {
      val kv = (s, st.size)
      st = st + kv
    }
    st
  }

  def invertIndexKeys(st: TreeMap[String, Int]) = {
    val keys = new Array[String](st.size)
    for (name <- st.keys) {
      val keyOpt = st.get(name)
      keyOpt match {
        case Some(x) => keys(x) = name
        case None =>
      }
    }
    keys
  }
}

trait IntArrayBuilder extends BufferedSourceBuilder {
  
  def readFileToArray(buffSource: BufferedSource): ArrayBuffer[Int] = {
    val savedLines = new ArrayBuffer[Int]()
    val it = buffSource.getLines
    for {
      a <- it
      i <- a.split("\\s+")
    } savedLines.append(i.toInt)
    savedLines
  }

  def buildFromManagedResource(uri: String): ArrayBuffer[Int] = {
    val managedResource = readURI(uri)
    managedResource.loan(readFileToArray)
  }
}