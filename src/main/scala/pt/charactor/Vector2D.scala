package pt.charactor

case class Vector2D(x: Double, y: Double)
{
   def + (other: Vector2D): Vector2D = new Vector2D(x + other.x, y + other.y)
   def + (value: Double): Vector2D = new Vector2D(x + value, y + value)
   def - (other: Vector2D): Vector2D = new Vector2D(x - other.x, y - other.y)
   def - (value: Double): Vector2D = new Vector2D(x - value, y - value)
   def * (multiplier: Double): Vector2D = new Vector2D(x*multiplier, y*multiplier)
   def == (other: Vector2D): Boolean = (x == other.x) && (y == other.y)

   def / (divider: Double): Vector2D =
   {
       new Vector2D(x/divider, y/divider)
   }

   def % (divider: Double): Vector2D =
   {
         new Vector2D(x%divider, y%divider)
   }

   def % (other: Vector2D): Vector2D =
   {
      var xMod = x
      var yMod = y
      if (other.x != 0)
         xMod = x%other.x

      if (other.y != 0)
         yMod = y%other.y

      new Vector2D(xMod, yMod)
   }

   def bounded(other: Vector2D) = {
     val v = this % other
     Vector2D(if(v.x > 0) v.x else other.x - v.x, if(v.y > 0) v.y else other.y - v.y)
   }

   def max(limit: Vector2D): Vector2D = new Vector2D(Math.max(x, limit.x), Math.max(y, limit.y))
   def max(limit: Double): Vector2D = new Vector2D(Math.max(x, limit), Math.max(y, limit))
   def min(limit: Vector2D): Vector2D = new Vector2D(Math.min(x, limit.x), Math.min(y, limit.y))
   def min(limit: Double): Vector2D = new Vector2D(Math.min(x, limit), Math.min(y, limit))

   def length(): Double = Math.sqrt(x*x + y*y)
   def normalize(): Vector2D = new Vector2D(x/length, y/length)

   def distanceFrom(other: Vector2D): Double =
   {
      val xDiff = x - other.x
      val yDiff = y - other.y

      Math.sqrt(xDiff*xDiff + yDiff*yDiff)
   }

  def rotateRadians(rad:Double) = {
    val ca = math.cos(rad)
    val sa = math.sin(rad)
    Vector2D(ca*x - sa*y, sa*x + ca*y)
  }
}
