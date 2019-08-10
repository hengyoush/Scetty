package channel

trait ChannelId extends Serializable with Comparable[ChannelId] {
  def asShortText: String
  def asLongText: String
}
