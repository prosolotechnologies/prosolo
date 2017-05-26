package org.prosolo.bigdata.scala.clustering.userprofiling

import org.apache.hadoop.fs.{Path, PathFilter}

/**
 * @author zoran October 24, 2015
 */
class ClustersFilter extends PathFilter{
override def accept(path: Path): Boolean = {
    val pathString = path.toString
    pathString.contains("/clusters-")
  }
}