package com.example.cateringapp.data.dto

import CommandeDTO

fun Commande.toDTO(): CommandeDTO {
    return CommandeDTO(
        id = this.id, // ✅ Très important
        nomClient = this.nomClient,
        salle = this.salle,
        nombreTables = this.nombreTables,
        prixParTable = this.prixParTable,
        typeClient = this.typeClient,
        typeCommande = this.typeCommande,
        statut = this.statut,
        date = this.date,
        objet = this.objet,
        commentaire = commentaire,
        produits = this.produits // ✅ Et pas emptyList()
    )
}

