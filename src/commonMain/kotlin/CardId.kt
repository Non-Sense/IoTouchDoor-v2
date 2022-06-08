import kotlinx.serialization.Serializable

@Serializable
enum class CardIdType {
    Felica,
    GunmaUniv,
    Unknown
}

@Serializable
data class CardId(
    val type: CardIdType,
    val id: String
) {
    companion object {
        fun determineType(id: String): CardId {
            if(id.length == 71 && id.startsWith("%G1016") && id.endsWith("?"))
                return CardId(CardIdType.GunmaUniv, id.slice(19..26))
            if(id.length == 16)
                return CardId(CardIdType.Felica, id)
            return CardId(CardIdType.Unknown, id)
        }
    }
}