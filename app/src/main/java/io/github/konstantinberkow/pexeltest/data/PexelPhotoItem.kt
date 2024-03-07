package io.github.konstantinberkow.pexeltest.data

data class PexelPhotoItem(
    val id: Long,
    val photographerName: String,
    val srcSmall: String,
    val srcLarge: String,
    val averageColor: String,
) {
    companion object {
        val MOCK_ITEMS: List<PexelPhotoItem> = listOf(
            PexelPhotoItem(
                id = 20423561L,
                photographerName = "Artur Stec",
                srcSmall = "https://images.pexels.com/photos/20423561/pexels-photo-20423561.jpeg?auto=compress&cs=tinysrgb&h=130",
                srcLarge = "https://images.pexels.com/photos/20423561/pexels-photo-20423561.jpeg?auto=compress&cs=tinysrgb&h=650&w=940",
                averageColor = "#7F6355"
            ),
            PexelPhotoItem(
                id = 20385938L,
                photographerName = "Mathias Reding",
                srcSmall = "https://images.pexels.com/photos/20385938/pexels-photo-20385938.jpeg?auto=compress&cs=tinysrgb&h=130",
                srcLarge = "https://images.pexels.com/photos/20385938/pexels-photo-20385938.jpeg?auto=compress&cs=tinysrgb&h=650&w=940",
                averageColor = "#B2B4B1"
            ),
        )
    }
}
