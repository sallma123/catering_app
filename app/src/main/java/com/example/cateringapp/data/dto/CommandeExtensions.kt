package com.example.cateringapp.data.dto

import CommandeDTO

fun Commande.toDTO(): CommandeDTO {
    return CommandeDTO(
        nomClient = this.nomClient,
        salle = this.salle,
        nombreTables = this.nombreTables,
        prixParTable = this.prixParTable,
        typeClient = this.typeClient,
        typeCommande = this.typeCommande,
        statut = this.statut,
        date = this.date,
        produits = emptyList() // tu mettras la vraie liste si elle existe dans Commande
    )
}
