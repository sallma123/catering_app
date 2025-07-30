import com.example.cateringapp.data.dto.ProduitCommande
import java.io.Serializable

data class CommandeDTO(
    val id: Long? = null,  // ✅ ID ajouté ici
    val nomClient: String,
    val salle: String,
    val nombreTables: Int,
    val prixParTable: Double,
    val typeClient: String,
    val typeCommande: String,
    val statut: String,
    val date: String,
    val produits: List<ProduitCommande> = emptyList(),
    var objet: String? = null,
    val commentaire: String? = null

) : Serializable
