package scromium.meta

class ColumnFamilyBuilder(val keyspace : String, val name : String) {
  var comparatorType = "BytesType"
  var rowCacheSize = 0.0
  var preloadRowCache = false
  var keyCacheSize = 200000.0
  var readRepairChance = 1.0
  var gcGraceSeconds = 0
  
  def apply[T](f : this.type => T) : T = f(this)
  
  
  
  def comparatorType(t : String) : this.type = {
    comparatorType = t
    this
  }
   
  
  def rowCacheSize(s : Double) : this.type = {
    rowCacheSize = s
    this
  }
  
  def preloadRowCache(b : Boolean) : this.type = {
    preloadRowCache = b
    this
  }
  
  def keyCacheSize(s : Double) : this.type = {
    keyCacheSize = s
    this
  }
  
  def readRepairChance(s : Double) : this.type = {
    readRepairChance = s
    this
  }
  
  def gcGraceSeconds(i : Int) : this.type = {
    gcGraceSeconds = i
    this
  }
  
  def toDefinition = ColumnFamilyDef(keyspace, name, "Standard",
    comparatorType, "",
    "", rowCacheSize,
    preloadRowCache, keyCacheSize, readRepairChance,
    gcGraceSeconds)
}

class SuperColumnFamilyBuilder(keyspace : String, name : String) extends ColumnFamilyBuilder(keyspace, name) {
  var subComparatorType = ""
  
  def subComparatorType(t : String) : this.type = {
    subComparatorType = t
    this
  }
  
  override def toDefinition = ColumnFamilyDef(keyspace, name, "Super",
     comparatorType, subComparatorType,
     "", rowCacheSize,
    preloadRowCache, keyCacheSize, readRepairChance,
    gcGraceSeconds)
}