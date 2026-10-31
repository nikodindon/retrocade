package com.nikodindon.retrocade.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.billing.BillingProduct
import com.nikodindon.retrocade.core.billing.BillingProductType
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.PixelDivider
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.core.ui.RetroHeader
import com.nikodindon.retrocade.Links
import com.nikodindon.retrocade.navigation.findActivity

@Composable
fun PaywallScreen(onBack: () -> Unit) {
    val viewModel: PaywallViewModel = hiltViewModel()
    val premium by viewModel.premium.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val initialized by viewModel.initialized.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity: Activity? = remember(context) { context.findActivity() }
    val uriHandler = LocalUriHandler.current

    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            RetroHeader(
                title = "Boutique",
                subtitle = "Soutenir Rétrocade",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {
                PremiumStatus(premium = premium)

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "AVANTAGES",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.NeonCyan,
                )
                Spacer(Modifier.height(8.dp))
                NeonPanel(Modifier.fillMaxWidth(), accent = RetroColors.Amber) {
                    Column {
                        Benefit("Aucun écran publicitaire entre les parties")
                        Benefit("Accès immédiat, même hors ligne")
                        Benefit("Le jeu reste entier : aucun contenu verrouillé")
                        Benefit("Un projet indépendant, sans éditeur derrière")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "OFFRES",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.NeonCyan,
                )
                Spacer(Modifier.height(8.dp))

                val lifetime = products.firstOrNull { it.type == BillingProductType.LIFETIME }

                ProductCard(
                    title = "Premium à vie",
                    description = "Un paiement unique. Aucun abonnement, aucun renouvellement.",
                    product = lifetime,
                    accent = RetroColors.Amber,
                    action = "Acheter",
                    busy = viewModel.busy,
                    premium = premium,
                    initialized = initialized,
                    onPurchase = {
                        activity?.let { viewModel.purchase(it, BillingProductType.LIFETIME) }
                    },
                )

                Spacer(Modifier.height(14.dp))
                RetroButton(
                    label = "Restaurer mes achats",
                    onClick = viewModel::restore,
                    accent = RetroColors.TextMuted,
                    enabled = !viewModel.busy,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth(),
                )

                viewModel.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroColors.NeonCyan,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))
                PixelDivider()
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Les transactions sont traitées par Google Play. Rétrocade ne stocke aucune donnée de paiement. " +
                        "Le statut Premium est vérifié à chaque lancement et conservé localement en cas de coupure réseau.",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                )
                Spacer(Modifier.height(10.dp))
                RetroButton(
                    label = "Politique de confidentialité",
                    onClick = { uriHandler.openUri(Links.PRIVACY_POLICY) },
                    accent = RetroColors.TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun PremiumStatus(premium: PremiumState) {
    val active = premium is PremiumState.Premium
    val accent = if (active) RetroColors.Amber else RetroColors.TextDim
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.Star,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = when (premium) {
                        PremiumState.Unknown -> "VÉRIFICATION DU STATUT…"
                        PremiumState.Free -> "OFFRE GRATUITE"
                        is PremiumState.Premium -> "PREMIUM ACTIF"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (active) RetroColors.Amber else RetroColors.TextPrimary,
                )
                Text(
                    text = when (premium) {
                        PremiumState.Unknown -> "Connexion à Google Play en cours."
                        PremiumState.Free -> "Les interstitiels peuvent apparaître entre deux parties."
                        is PremiumState.Premium -> "Achat à vie · aucune publicité."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun Benefit(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.titleMedium,
            color = RetroColors.Amber,
            modifier = Modifier.width(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = RetroColors.TextPrimary,
        )
    }
}

@Composable
private fun ProductCard(
    title: String,
    description: String,
    product: BillingProduct?,
    accent: androidx.compose.ui.graphics.Color,
    action: String,
    busy: Boolean,
    premium: PremiumState,
    initialized: Boolean,
    onPurchase: () -> Unit,
) {
    val owned = premium is PremiumState.Premium
    NeonPanel(Modifier.fillMaxWidth(), accent = accent) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = RetroColors.TextPrimary,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroColors.TextMuted,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = product?.formattedPrice ?: if (initialized) "INDISPONIBLE" else "…",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
            }
            Spacer(Modifier.height(12.dp))
            RetroButton(
                label = if (owned) "Déjà premium" else action,
                onClick = onPurchase,
                accent = accent,
                enabled = !busy && !owned && product != null,
                icon = Icons.Default.ShoppingCart,
                modifier = Modifier.fillMaxWidth(),
            )
            if (busy) {
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = accent,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Communication avec Google Play…",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroColors.TextMuted,
                    )
                }
            }
        }
    }
}
