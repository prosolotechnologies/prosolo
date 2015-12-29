package org.prosolo.bigdata.scala.clustering.userprofiling

import java.util

import com.google.common.collect.Lists
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{LongWritable, SequenceFile}
import org.apache.mahout.clustering.Cluster
import org.apache.mahout.clustering.iterator.ClusterWritable
import org.apache.mahout.common.iterator.sequencefile.{PathFilters, PathType, SequenceFileDirValueIterable}
import org.apache.mahout.math.{Vector, VectorWritable}

import scala.collection.JavaConverters._

/**
 * @author zoran October 24, 2015
 */
object ClusterHelper {
def writePointsToFile(points: util.List[Vector], conf: Configuration, path: Path): Unit = {
    val fs = FileSystem.get(path.toUri, conf)
    val writer = new SequenceFile.Writer(fs, conf, path, classOf[LongWritable], classOf[VectorWritable])
    var recNum = 0L
    val vec = new VectorWritable()
    points.asScala.foreach { point =>
      vec.set(point)
      writer.append(new LongWritable(recNum), vec)
      recNum += 1
    }
    writer.close()
  }

  def readClusters(conf: Configuration, output: Path): util.List[util.List[Cluster]] = {
    val clustersList = Lists.newArrayList[util.List[Cluster]]()
    val fs = FileSystem.get(output.toUri, conf)

    fs.listStatus(output, new ClustersFilter()).foreach { s =>
      val clusters = Lists.newArrayList[Cluster]()
      val iterable = new SequenceFileDirValueIterable[ClusterWritable](s.getPath,
                                                                        PathType.LIST,
                                                                        PathFilters.logsCRCFilter,
                                                                        conf)
      iterable.asScala.foreach { value =>
        val cluster = value.getValue
        clusters.add(cluster)
      }
      clustersList.add(clusters)
    }

    clustersList
  }
}