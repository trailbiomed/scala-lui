package lui

import scala.scalajs.js

/** Calendar date — year, month (1..12), day-of-month (1..31). No time, no zone.
  *
  * Uses `js.Date` internally for week-of-day and epoch arithmetic, so it works in
  * the browser without pulling in a `java.time` polyfill. */
final case class Day(year: Int, month: Int, day: Int) extends Ordered[Day] {
  def compare(that: Day): Int =
    if (year != that.year) year - that.year
    else if (month != that.month) month - that.month
    else day - that.day

  /** ISO-8601: `"YYYY-MM-DD"`. */
  def iso: String = f"$year%04d-$month%02d-$day%02d"

  override def toString: String = iso

  def daysInMonth: Int = Day.daysInMonth(year, month)

  def firstOfMonth: Day = Day(year, month, 1)

  /** 0=Sun, 1=Mon, …, 6=Sat. */
  def dayOfWeekSun: Int = {
    val d = new js.Date(js.Date.UTC(year, month - 1, day))
    d.getUTCDay().toInt
  }

  /** 0=Mon, 1=Tue, …, 6=Sun. */
  def dayOfWeekMon: Int = (dayOfWeekSun + 6) % 7

  def addDays(n: Int): Day = Day.ofEpochDay(toEpochDay + n)

  /** Same day-of-month in the new month, clamped to that month's length. */
  def addMonths(n: Int): Day = {
    val total = year.toLong * 12 + (month - 1) + n
    val ny = Math.floorDiv(total, 12).toInt
    val nm = Math.floorMod(total, 12).toInt + 1
    val nd = math.min(day, Day.daysInMonth(ny, nm))
    Day(ny, nm, nd)
  }

  def toEpochDay: Long = {
    val ms = js.Date.UTC(year, month - 1, day)
    (ms / 86400000.0).toLong
  }
}

object Day {
  private def isLeap(y: Int): Boolean =
    (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)

  private val monthLengths = Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

  def daysInMonth(year: Int, month: Int): Int =
    if (month == 2 && isLeap(year)) 29 else monthLengths(month - 1)

  /** Today in local time. */
  def today: Day = {
    val d = new js.Date()
    Day(d.getFullYear().toInt, d.getMonth().toInt + 1, d.getDate().toInt)
  }

  def ofEpochDay(epoch: Long): Day = {
    val d = new js.Date(epoch.toDouble * 86400000.0)
    Day(d.getUTCFullYear().toInt, d.getUTCMonth().toInt + 1, d.getUTCDate().toInt)
  }

  /** Parse `"YYYY-MM-DD"`. Returns `None` for malformed or out-of-range strings. */
  def fromIso(s: String): Option[Day] = {
    if (s == null || s.length != 10 || s.charAt(4) != '-' || s.charAt(7) != '-') None
    else {
      try {
        val y = s.substring(0, 4).toInt
        val m = s.substring(5, 7).toInt
        val d = s.substring(8, 10).toInt
        if (m < 1 || m > 12) None
        else if (d < 1 || d > daysInMonth(y, m)) None
        else Some(Day(y, m, d))
      } catch { case _: Throwable => None }
    }
  }

  val monthNames: Vector[String] = Vector(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )

  val monthShort: Vector[String] = Vector(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  )

  /** `weekdaysMon(0) = "Mon"`, `weekdaysMon(6) = "Sun"`. */
  val weekdaysMon: Vector[String] = Vector("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

  /** `weekdaysSun(0) = "Sun"`, `weekdaysSun(6) = "Sat"`. */
  val weekdaysSun: Vector[String] = Vector("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
}
