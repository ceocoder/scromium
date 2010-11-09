package scromium.util

import org.slf4j.{ Logger, LoggerFactory }

trait Log {

  /**name of logger derived from class*/
  val loggerName = this.getClass.getName

  /**logger instance*/
  lazy val logger = LoggerFactory.getLogger(loggerName)

}
