package com.example.cateringapp.ui.screen.commandes

import androidx.compose.runtime.toMutableStateList
import com.example.cateringapp.data.dto.ProduitCommande
import com.example.cateringapp.data.dto.SectionProduit

fun getSectionsPourTypeCommande(typeCommande: String): List<SectionProduit> {
    val supplement = mutableListOf<ProduitCommande>()

    return when (typeCommande.uppercase()) {
        "BUFFET_DE_SOUTENANCE" -> listOf(
            SectionProduit("Côté sucré", listOf(
                ProduitCommande("Mini Soirées", "Côté sucré", 0.0),
                ProduitCommande("Mini tartelettes", "Côté sucré", 0.0),
                ProduitCommande("Gâteaux aux amandes", "Côté sucré", 0.0),
                ProduitCommande("Gâteaux Prestige", "Côté sucré", 0.0),
                ProduitCommande("Gâteaux Sablés", "Côté sucré", 0.0)
            ).toMutableStateList()),
            SectionProduit("Côté salé", listOf(
                ProduitCommande("Salé feuilleté (quiches, mini Burgers, mini chawarma..)", "Côté salé", 0.0),
                ProduitCommande("Salé marocains : pastillas poulet, pastillas poisson, briwates, ..", "Côté salé", 0.0),
                ProduitCommande("Sushis", "Côté salé", 0.0)
            ).toMutableStateList()),
            SectionProduit("Boisson", listOf(
                ProduitCommande("Jus trois parfums nature: Cocktail à l’orange, citron gingembre, fraise", "Boisson", 0.0),
                ProduitCommande("Café", "Boisson", 0.0),
                ProduitCommande("Thé", "Boisson", 0.0),
                ProduitCommande("Eau minérale", "Boisson", 0.0)
            ).toMutableStateList()),
            SectionProduit("Supplément", supplement.toMutableStateList())
        )
        "FTOUR_RAMADAN" -> listOf(
            SectionProduit("Soupes", listOf(
                ProduitCommande("Harira traditionnelle", "Soupes", 0.0),
                ProduitCommande("Soupe chinoise", "Soupes", 0.0)
            ).toMutableStateList()),
            SectionProduit("Accompagnements", listOf(
                ProduitCommande("Dattes, œufs durs", "Accompagnements", 0.0),
                ProduitCommande("Chebakia, sellou, Briouates aux amandes", "Accompagnements", 0.0),
                ProduitCommande("Mini viennoiserie", "Accompagnements", 0.0)
            ).toMutableStateList()),
            SectionProduit("Entremets", listOf(
                ProduitCommande("Sablé au saumon fumé et cream cheese au miel", "Entremets", 0.0),
                ProduitCommande("Toast à la mousse de foie gras", "Entremets", 0.0),
                ProduitCommande("Feuilletés croquants au gambas", "Entremets", 0.0),
                ProduitCommande("Nems viande hachée- crevette", "Entremets", 0.0),
                ProduitCommande("Briouates poulet épinard - printanière", "Entremets", 0.0),
                ProduitCommande("Quiches océanes", "Entremets", 0.0),
                ProduitCommande("Mliwyat nature, Mliwyat au khliaa et Mkhemrat farcies", "Entremets", 0.0),
                ProduitCommande("Pain, jben, fromage", "Entremets", 0.0)
            ).toMutableStateList()),
            SectionProduit("Jus et boissons chaudes", listOf(
                ProduitCommande("Jus 3 parfums : cocktail à l'orange, fraise, avocat", "Jus et boissons chaudes", 0.0),
                ProduitCommande("Lait froid et chaud", "Jus et boissons chaudes", 0.0),
                ProduitCommande("Café, thé et eau minérale", "Jus et boissons chaudes", 0.0)
            ).toMutableStateList()),
            SectionProduit("Tajines", listOf(
                ProduitCommande("Tajine de foie", "Tajines", 0.0),
                ProduitCommande("Tajine pil-pil", "Tajines", 0.0),
                ProduitCommande("Tajine cervelle", "Tajines", 0.0)
            ).toMutableStateList()),
            SectionProduit("Tradition", listOf(
                ProduitCommande("Rzira et beghrir", "Tradition", 0.0)
            ).toMutableStateList()),
            SectionProduit("Pause-thé", listOf(
                ProduitCommande("Thé avec Festival de gâteaux marocains (ghraiba aux noix et cornes de gazelle)", "Pause-thé", 0.0)
            ).toMutableStateList()),
            SectionProduit("Supplément", supplement.toMutableStateList())
        )
        else -> {
            val reception = mutableListOf(
                ProduitCommande("Dattes et lait", "Réception", 0.0),
                ProduitCommande("Amuses bouche", "Réception", 0.0),
                ProduitCommande("Bébé macaron et Mini cookies", "Réception", 0.0),
                ProduitCommande("Petits fours salés (2 types)", "Réception", 0.0),
                ProduitCommande("Amandes salés", "Réception", 0.0),
                ProduitCommande("Choux caramélisés", "Réception", 0.0),
                ProduitCommande("Sushis", "Réception", 0.0)
            )
            val cocktail = mutableListOf(
                ProduitCommande("Jus parfums nature (fraise, citron gingembre, avocat à orange)", "Cocktail d’accueil servi à table", 0.0),
                ProduitCommande("Gâteaux soirée", "Cocktail d’accueil servi à table", 0.0),
                ProduitCommande("Gâteaux prestige", "Cocktail d’accueil servi à table", 0.0)
            )
            val entremets = mutableListOf(
                ProduitCommande("Nems viande hachée", "Entremets", 0.0),
                ProduitCommande("Verrines crevette", "Entremets", 0.0),
                ProduitCommande("Briouates poulet épinard", "Entremets", 0.0),
                ProduitCommande("Coquillages poisson blanc", "Entremets", 0.0),
                ProduitCommande("Coquillages aigre doux", "Entremets", 0.0),
                ProduitCommande("Cake salé", "Entremets", 0.0)
            )
            val festival = mutableListOf(
                ProduitCommande("Thé", "Festival de gâteaux beldi", 0.0),
                ProduitCommande("4 Gâteaux amandes : k3ab, Kehk, ghraiba aux noix, la lune à l’orange", "Festival de gâteaux beldi", 0.0)
            )
            val diner = mutableListOf(
                ProduitCommande("Trio salades: quinoa, Boruta et Guacamole", "Diner", 0.0),
                ProduitCommande("Poulet dermira", "Diner", 0.0),
                ProduitCommande("Duo pastillas", "Diner", 0.0),
                ProduitCommande("Pastilla Poisson royale", "Diner", 0.0),
                ProduitCommande("Mechwi d'agneau avec garniture", "Diner", 0.0),
                ProduitCommande("Chwa malaki avec garniture", "Diner", 0.0),
                ProduitCommande("Tajine de veau contemporain", "Diner", 0.0),
                ProduitCommande("Accompagnement :assortiment de salades marocaines (Zaalouk, Beqola épinard, potiror caramélisé, cervelle, foi à la chermola)", "Diner", 0.0)
            )
            val dessert = mutableListOf(
                ProduitCommande("Corbeille de fruits", "Côté dessert", 0.0),
                ProduitCommande("Gâteau glacé", "Côté dessert", 0.0)
            )
            val apresdiner = mutableListOf(
                ProduitCommande("Café", "Après Diner", 0.0),
                ProduitCommande("Gâteaux au miel : briwat, mhencha", "Après Diner", 0.0)
            )

            listOf(
                SectionProduit("Réception", reception.toMutableStateList()),
                SectionProduit("Cocktail d’accueil servi à table", cocktail.toMutableStateList()),
                SectionProduit("Entremets", entremets.toMutableStateList()),
                SectionProduit("Festival de gâteaux beldi", festival.toMutableStateList()),
                SectionProduit("Diner", diner.toMutableStateList()),
                SectionProduit("Côté dessert", dessert.toMutableStateList()),
                SectionProduit("Après Diner", apresdiner.toMutableStateList()),
                SectionProduit("Supplément", supplement.toMutableStateList())
            )
        }
    }
}
