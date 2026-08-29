package com.example.ui.components.doodles

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class BotanicalPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val stem: Color = Color(0xFF2E7D32),
    val leaf: Color = SproutLeaf,
    val leafDark: Color = SproutLeafDark,
    val highlight: Color = Color.White.copy(alpha = 0.5f),
    val variantTag: String = "standard"
)

object ProduceColorResolver {

    fun resolveColors(name: String, archetype: String, symbolId: String?): BotanicalPalette {
        val lower = name.lowercase().trim()

        // 1. Apples & Pomes
        if (symbolId == "d-apple" || lower.contains("apple") || lower.contains("crabapple") || (archetype == "pome" && !lower.contains("pear") && !lower.contains("quince"))) {
            return when {
                lower.contains("granny smith") || lower.contains("green") || lower.contains("bramley") ->
                    BotanicalPalette(
                        primary = Color(0xFF7CB342),
                        secondary = Color(0xFFAED581),
                        accent = Color(0xFF558B2F),
                        variantTag = "green_apple"
                    )
                lower.contains("golden delicious") || lower.contains("yellow") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFCA28),
                        secondary = Color(0xFFFFF176),
                        accent = Color(0xFFFFB300),
                        variantTag = "yellow_apple"
                    )
                lower.contains("pink lady") || lower.contains("cripps") ->
                    BotanicalPalette(
                        primary = Color(0xFFEF5350),
                        secondary = Color(0xFFFF8A80),
                        accent = Color(0xFFFFD54F),
                        variantTag = "pink_apple"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFFD63B2F),
                        secondary = Color(0xFFFFB300).copy(alpha = 0.55f),
                        accent = Color(0xFFB71C1C),
                        variantTag = "red_apple"
                    )
            }
        }

        // 2. Bell Peppers & Chilies
        if (lower.contains("pepper") || lower.contains("chili") || lower.contains("jalapeño") || lower.contains("serrano") || lower.contains("poblano") || lower.contains("habanero")) {
            return when {
                lower.contains("yellow") || lower.contains("banana pepper") || lower.contains("wax pepper") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFD54F),
                        secondary = Color(0xFFFFF176),
                        accent = Color(0xFFF57F17),
                        variantTag = "yellow_pepper"
                    )
                lower.contains("orange") || lower.contains("habanero") ->
                    BotanicalPalette(
                        primary = Color(0xFFFF9800),
                        secondary = Color(0xFFFFB74D),
                        accent = Color(0xFFE65100),
                        variantTag = "orange_pepper"
                    )
                lower.contains("green") || lower.contains("jalapeño") || lower.contains("serrano") || lower.contains("poblano") || lower.contains("shishito") || lower.contains("padrón") || lower.contains("cubanelle") ->
                    BotanicalPalette(
                        primary = Color(0xFF2E7D32),
                        secondary = Color(0xFF4CAF50),
                        accent = Color(0xFF1B5E20),
                        variantTag = "green_pepper"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFFE53935),
                        secondary = Color(0xFFEF5350),
                        accent = Color(0xFFB71C1C),
                        variantTag = "red_pepper"
                    )
            }
        }

        // 3. Onions & Alliums
        if (lower.contains("onion") || lower.contains("shallot") || lower.contains("garlic") || lower.contains("leek") || lower.contains("scallion") || lower.contains("chive") || lower.contains("ramp")) {
            return when {
                lower.contains("red onion") || lower.contains("shallot") || lower.contains("purple") ->
                    BotanicalPalette(
                        primary = Color(0xFF8E24AA),
                        secondary = Color(0xFFBA68C8),
                        accent = Color(0xFF4A148C),
                        stem = Color(0xFF689F38),
                        variantTag = "red_onion"
                    )
                lower.contains("white onion") || lower.contains("pearl onion") || lower.contains("garlic") ->
                    BotanicalPalette(
                        primary = Color(0xFFF9F7EB),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFD7CCC8),
                        stem = Color(0xFF8D6E63),
                        variantTag = "white_allium"
                    )
                lower.contains("scallion") || lower.contains("green onion") || lower.contains("leek") || lower.contains("chives") || lower.contains("ramp") ->
                    BotanicalPalette(
                        primary = Color(0xFF4CAF50),
                        secondary = Color(0xFF81C784),
                        accent = Color(0xFFF9F7EB),
                        stem = Color(0xFF2E7D32),
                        variantTag = "green_allium"
                    )
                else ->
                    // Yellow / Sweet Onion
                    BotanicalPalette(
                        primary = Color(0xFFE6C687),
                        secondary = Color(0xFFF5DEB3),
                        accent = Color(0xFFB8860B),
                        stem = Color(0xFF8D6E63),
                        variantTag = "yellow_onion"
                    )
            }
        }

        // 4. Cabbage & Cauliflower & Crucifers
        if (lower.contains("cabbage") || lower.contains("kohlrabi")) {
            return when {
                lower.contains("red") || lower.contains("purple") ->
                    BotanicalPalette(
                        primary = Color(0xFF7B1FA2),
                        secondary = Color(0xFFAB47BC),
                        accent = Color(0xFF4A148C),
                        variantTag = "red_cabbage"
                    )
                lower.contains("napa") || lower.contains("bok choy") || lower.contains("savoy") ->
                    BotanicalPalette(
                        primary = Color(0xFFAED581),
                        secondary = Color(0xFFC5E1A5),
                        accent = Color(0xFF558B2F),
                        variantTag = "napa_cabbage"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFF81C784),
                        secondary = Color(0xFFA5D6A7),
                        accent = Color(0xFF388E3C),
                        variantTag = "green_cabbage"
                    )
            }
        }

        if (lower.contains("cauliflower") || lower.contains("romanesco")) {
            return when {
                lower.contains("purple") ->
                    BotanicalPalette(
                        primary = Color(0xFF8E24AA),
                        secondary = Color(0xFFBA68C8),
                        accent = Color(0xFF4A148C),
                        variantTag = "purple_cauliflower"
                    )
                lower.contains("orange") || lower.contains("cheddar") ->
                    BotanicalPalette(
                        primary = Color(0xFFFF9800),
                        secondary = Color(0xFFFFB74D),
                        accent = Color(0xFFE65100),
                        variantTag = "orange_cauliflower"
                    )
                lower.contains("romanesco") ->
                    BotanicalPalette(
                        primary = Color(0xFF8BC34A),
                        secondary = Color(0xFFAED581),
                        accent = Color(0xFF558B2F),
                        variantTag = "romanesco"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFFF9F9F4),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFE0E0D1),
                        variantTag = "white_cauliflower"
                    )
            }
        }

        // 5. Carrots
        if (lower.contains("carrot")) {
            return when {
                lower.contains("purple") ->
                    BotanicalPalette(
                        primary = Color(0xFF4A148C),
                        secondary = Color(0xFF7B1FA2),
                        accent = Color(0xFFFF6D00),
                        variantTag = "purple_carrot"
                    )
                lower.contains("yellow") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFD54F),
                        secondary = Color(0xFFFFF176),
                        accent = Color(0xFFFFA000),
                        variantTag = "yellow_carrot"
                    )
                lower.contains("white") ->
                    BotanicalPalette(
                        primary = Color(0xFFF5F0E1),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFD7CCC8),
                        variantTag = "white_carrot"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFFFF6D00),
                        secondary = Color(0xFFFFAB40),
                        accent = Color(0xFFD84315),
                        variantTag = "orange_carrot"
                    )
            }
        }

        // 6. Beets & Radishes & Turnips
        if (lower.contains("beet") || lower.contains("radish") || lower.contains("turnip") || lower.contains("rutabaga") || lower.contains("daikon") || lower.contains("parsnip") || lower.contains("horseradish")) {
            return when {
                lower.contains("golden beet") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFB300),
                        secondary = Color(0xFFFFD54F),
                        accent = Color(0xFFFF8F00),
                        leaf = Color(0xFF8BC34A),
                        variantTag = "golden_beet"
                    )
                lower.contains("daikon") || lower.contains("parsnip") || lower.contains("horseradish") ->
                    BotanicalPalette(
                        primary = Color(0xFFF7F7F0),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFE0E0D1),
                        leaf = Color(0xFF4CAF50),
                        variantTag = "white_root"
                    )
                lower.contains("turnip") || lower.contains("rutabaga") ->
                    BotanicalPalette(
                        primary = Color(0xFF8E24AA),
                        secondary = Color(0xFFFAFAF5),
                        accent = Color(0xFF4A148C),
                        leaf = Color(0xFF43A047),
                        variantTag = "turnip_purple_top"
                    )
                lower.contains("radish") ->
                    BotanicalPalette(
                        primary = Color(0xFFE91E63),
                        secondary = Color(0xFFFAFAF5),
                        accent = Color(0xFFC2185B),
                        leaf = Color(0xFF4CAF50),
                        variantTag = "radish_bicolor"
                    )
                else ->
                    // Red Beetroot
                    BotanicalPalette(
                        primary = Color(0xFF880E4F),
                        secondary = Color(0xFFAD1457),
                        accent = Color(0xFF4A148C),
                        leaf = Color(0xFF2E7D32),
                        leafDark = Color(0xFF6A1B9A),
                        variantTag = "red_beet"
                    )
            }
        }

        // 7. Potatoes & Sweet Potatoes & Tubers
        if (lower.contains("potato") || lower.contains("yam") || lower.contains("taro") || lower.contains("cassava") || lower.contains("yuca") || lower.contains("lotus root") || lower.contains("jicama")) {
            return when {
                lower.contains("purple sweet potato") || lower.contains("all blue") ->
                    BotanicalPalette(
                        primary = Color(0xFF512DA8),
                        secondary = Color(0xFF7E57C2),
                        accent = Color(0xFF311B92),
                        variantTag = "purple_potato"
                    )
                lower.contains("sweet potato") || lower.contains("satsumaimo") || lower.contains("beauregard") ->
                    BotanicalPalette(
                        primary = Color(0xFFD84315),
                        secondary = Color(0xFFFF7043),
                        accent = Color(0xFFBF360C),
                        variantTag = "sweet_potato"
                    )
                lower.contains("red bliss") ->
                    BotanicalPalette(
                        primary = Color(0xFFD32F2F),
                        secondary = Color(0xFFEF5350),
                        accent = Color(0xFFB71C1C),
                        variantTag = "red_potato"
                    )
                lower.contains("yukon gold") || lower.contains("fingerling") ->
                    BotanicalPalette(
                        primary = Color(0xFFDDB06C),
                        secondary = Color(0xFFFFD54F),
                        accent = Color(0xFFB8860B),
                        variantTag = "golden_potato"
                    )
                else ->
                    // Russet / Earthy potato
                    BotanicalPalette(
                        primary = Color(0xFFC69A64),
                        secondary = Color(0xFFDDB98B),
                        accent = Color(0xFF8D6E63),
                        variantTag = "russet_potato"
                    )
            }
        }

        // 8. Eggplants
        if (lower.contains("eggplant") || lower.contains("aubergine")) {
            return when {
                lower.contains("white") ->
                    BotanicalPalette(
                        primary = Color(0xFFF9F9F4),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFE0E0D1),
                        stem = Color(0xFF2E7D32),
                        variantTag = "white_eggplant"
                    )
                lower.contains("thai") ->
                    BotanicalPalette(
                        primary = Color(0xFF66BB6A),
                        secondary = Color(0xFFF5F5F0),
                        accent = Color(0xFF2E7D32),
                        stem = Color(0xFF1B5E20),
                        variantTag = "thai_eggplant"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFF4A148C),
                        secondary = Color(0xFF7B1FA2),
                        accent = Color(0xFF311B92),
                        stem = Color(0xFF2E7D32),
                        variantTag = "purple_eggplant"
                    )
            }
        }

        // 9. Grapes
        if (lower.contains("grape") && !lower.contains("grapefruit")) {
            return when {
                lower.contains("green") || lower.contains("thompson") || lower.contains("cotton candy") || lower.contains("muscat") ->
                    BotanicalPalette(
                        primary = Color(0xFF8BC34A),
                        secondary = Color(0xFFAED581),
                        accent = Color(0xFF689F38),
                        variantTag = "green_grapes"
                    )
                lower.contains("red") || lower.contains("crimson") || lower.contains("flame") ->
                    BotanicalPalette(
                        primary = Color(0xFFC2185B),
                        secondary = Color(0xFFE91E63),
                        accent = Color(0xFF880E4F),
                        variantTag = "red_grapes"
                    )
                else ->
                    // Concord / Purple Grapes
                    BotanicalPalette(
                        primary = Color(0xFF6A1B9A),
                        secondary = Color(0xFF9C27B0),
                        accent = Color(0xFF4A148C),
                        variantTag = "purple_grapes"
                    )
            }
        }

        // 10. Berries
        if (lower.contains("berry") || lower.contains("currant")) {
            return when {
                lower.contains("blueberry") || lower.contains("bilberry") || lower.contains("huckleberry") ->
                    BotanicalPalette(
                        primary = Color(0xFF283593),
                        secondary = Color(0xFF7986CB),
                        accent = Color(0xFF1A237E),
                        variantTag = "blueberry"
                    )
                lower.contains("blackberry") || lower.contains("black raspberry") || lower.contains("blackcurrant") || lower.contains("elderberry") || lower.contains("dewberry") || lower.contains("boysenberry") || lower.contains("marionberry") ->
                    BotanicalPalette(
                        primary = Color(0xFF212121),
                        secondary = Color(0xFF424242),
                        accent = Color(0xFF000000),
                        variantTag = "blackberry"
                    )
                lower.contains("raspberry") || lower.contains("loganberry") || lower.contains("tayberry") ->
                    BotanicalPalette(
                        primary = Color(0xFFE91E63),
                        secondary = Color(0xFFF48FB1),
                        accent = Color(0xFFAD1457),
                        variantTag = "raspberry"
                    )
                lower.contains("strawberry") ->
                    if (lower.contains("pineberry"))
                        BotanicalPalette(primary = Color(0xFFFFF8E1), secondary = Color(0xFFFFFFFF), accent = Color(0xFFE53935), variantTag = "pineberry")
                    else
                        BotanicalPalette(primary = Color(0xFFE52D27), secondary = Color(0xFFFF7043), accent = Color(0xFFB71C1C), variantTag = "strawberry")
                lower.contains("goldenberry") || lower.contains("cloudberry") || lower.contains("salmonberry") || lower.contains("cape gooseberry") ->
                    BotanicalPalette(
                        primary = Color(0xFFFF9800),
                        secondary = Color(0xFFFFD54F),
                        accent = Color(0xFFF57F17),
                        variantTag = "goldenberry"
                    )
                lower.contains("white currant") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFF9C4),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFFFEE58),
                        variantTag = "white_currant"
                    )
                lower.contains("green") && lower.contains("gooseberry") ->
                    BotanicalPalette(
                        primary = Color(0xFF8BC34A),
                        secondary = Color(0xFFAED581),
                        accent = Color(0xFF689F38),
                        variantTag = "green_gooseberry"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFFB71C1C),
                        secondary = Color(0xFFEF5350),
                        accent = Color(0xFF880E4F),
                        variantTag = "red_berry"
                    )
            }
        }

        // 11. Citrus
        if (lower.contains("lemon") && !lower.contains("cucumber")) {
            return BotanicalPalette(
                primary = Color(0xFFFFEB3B),
                secondary = Color(0xFFFFF59D),
                accent = Color(0xFFFBC02D),
                variantTag = "lemon"
            )
        }
        if (lower.contains("lime")) {
            return BotanicalPalette(
                primary = Color(0xFF7CB342),
                secondary = Color(0xFFAED581),
                accent = Color(0xFF558B2F),
                variantTag = "lime"
            )
        }
        if (lower.contains("grapefruit") || lower.contains("pomelo")) {
            return BotanicalPalette(
                primary = Color(0xFFFF8A65),
                secondary = Color(0xFFFFAB91),
                accent = Color(0xFFD84315),
                variantTag = "grapefruit"
            )
        }
        if (lower.contains("blood orange")) {
            return BotanicalPalette(
                primary = Color(0xFFC62828),
                secondary = Color(0xFFFF7043),
                accent = Color(0xFF880E4F),
                variantTag = "blood_orange"
            )
        }
        if (lower.contains("orange") || lower.contains("tangerine") || lower.contains("mandarin") || lower.contains("clementine") || lower.contains("kumquat")) {
            return BotanicalPalette(
                primary = Color(0xFFFF9800),
                secondary = Color(0xFFFFB74D),
                accent = Color(0xFFE65100),
                variantTag = "orange"
            )
        }

        // 12. Melons
        if (lower.contains("watermelon")) {
            return BotanicalPalette(
                primary = Color(0xFFE53935),
                secondary = Color(0xFF2C5E28),
                accent = Color(0xFFD2E6AF),
                variantTag = "watermelon"
            )
        }
        if (lower.contains("cantaloupe") || lower.contains("charentais")) {
            return BotanicalPalette(
                primary = Color(0xFFFFB74D),
                secondary = Color(0xFFFFCC80),
                accent = Color(0xFFF57F17),
                variantTag = "cantaloupe"
            )
        }
        if (lower.contains("honeydew")) {
            return BotanicalPalette(
                primary = Color(0xFFC5E1A5),
                secondary = Color(0xFFDCEDC8),
                accent = Color(0xFF8BC34A),
                variantTag = "honeydew"
            )
        }
        if (lower.contains("canary melon") || lower.contains("chamoe")) {
            return BotanicalPalette(
                primary = Color(0xFFFFEE58),
                secondary = Color(0xFFFFF9C4),
                accent = Color(0xFFFDD835),
                variantTag = "yellow_melon"
            )
        }

        // 13. Kiwifruit
        if (lower.contains("kiwi")) {
            return if (lower.contains("gold") || lower.contains("sungold") || lower.contains("yellow")) {
                BotanicalPalette(
                    primary = Color(0xFFFFD54F),
                    secondary = Color(0xFFFFF176),
                    accent = Color(0xFF8D6E63),
                    variantTag = "golden_kiwi"
                )
            } else {
                BotanicalPalette(
                    primary = Color(0xFF86C138),
                    secondary = Color(0xFF9BD946),
                    accent = Color(0xFF7A5731),
                    variantTag = "green_kiwi"
                )
            }
        }

        // 14. Dragonfruit / Pitaya
        if (lower.contains("dragon") || lower.contains("pitaya")) {
            return when {
                lower.contains("yellow") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFD54F),
                        secondary = Color(0xFFFFF176),
                        accent = Color(0xFF689F38),
                        variantTag = "yellow_dragonfruit"
                    )
                lower.contains("red dragon") ->
                    BotanicalPalette(
                        primary = Color(0xFFE91E63),
                        secondary = Color(0xFFC2185B),
                        accent = Color(0xFF8BC34A),
                        variantTag = "red_dragonfruit"
                    )
                else ->
                    BotanicalPalette(
                        primary = Color(0xFFE91E63),
                        secondary = Color(0xFFF8BBD0),
                        accent = Color(0xFF8BC34A),
                        variantTag = "white_dragonfruit"
                    )
            }
        }

        // 15. Mushrooms
        if (lower.contains("mushroom")) {
            return when {
                lower.contains("white") || lower.contains("button") || lower.contains("enoki") ->
                    BotanicalPalette(
                        primary = Color(0xFFF5F5F0),
                        secondary = Color(0xFFFFFFFF),
                        accent = Color(0xFFD7CCC8),
                        variantTag = "white_mushroom"
                    )
                lower.contains("chanterelle") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFB300),
                        secondary = Color(0xFFFFD54F),
                        accent = Color(0xFFFFA000),
                        variantTag = "chanterelle"
                    )
                lower.contains("oyster") ->
                    BotanicalPalette(
                        primary = Color(0xFFCFD8DC),
                        secondary = Color(0xFFECEFF1),
                        accent = Color(0xFF90A4AE),
                        variantTag = "oyster_mushroom"
                    )
                else ->
                    // Cremini / Portobello / Shiitake
                    BotanicalPalette(
                        primary = Color(0xFF8D6E63),
                        secondary = Color(0xFFA1887F),
                        accent = Color(0xFF5D4037),
                        variantTag = "brown_mushroom"
                    )
            }
        }

        // 16. Corn & Asparagus & Ginger & Turmeric
        if (lower.contains("corn")) {
            return BotanicalPalette(
                primary = Color(0xFFFFD54F),
                secondary = Color(0xFFFFF176),
                accent = Color(0xFF8BC34A),
                variantTag = "corn"
            )
        }
        if (lower.contains("ginger")) {
            return BotanicalPalette(
                primary = Color(0xFFD2B48C),
                secondary = Color(0xFFE8DCC0),
                accent = Color(0xFFC49A6C),
                variantTag = "ginger"
            )
        }
        if (lower.contains("turmeric")) {
            return BotanicalPalette(
                primary = Color(0xFFFF6D00),
                secondary = Color(0xFFFFB74D),
                accent = Color(0xFFE65100),
                variantTag = "turmeric"
            )
        }
        if (lower.contains("asparagus")) {
            return when {
                lower.contains("white") ->
                    BotanicalPalette(primary = Color(0xFFF5F5E6), secondary = Color(0xFFFFFFFF), accent = Color(0xFFD7CCC8), variantTag = "white_asparagus")
                lower.contains("purple") ->
                    BotanicalPalette(primary = Color(0xFF6A1B9A), secondary = Color(0xFF9C27B0), accent = Color(0xFF4A148C), variantTag = "purple_asparagus")
                else ->
                    BotanicalPalette(primary = Color(0xFF388E3C), secondary = Color(0xFF4CAF50), accent = Color(0xFF7B1FA2), variantTag = "green_asparagus")
            }
        }

        // 17. Squashes & Gourds & Zucchini
        if (lower.contains("zucchini") || lower.contains("courgette")) {
            return if (lower.contains("yellow") || lower.contains("golden")) {
                BotanicalPalette(primary = Color(0xFFFFD54F), secondary = Color(0xFFFFF176), accent = Color(0xFFFFA000), variantTag = "yellow_zucchini")
            } else {
                BotanicalPalette(primary = Color(0xFF2E7D32), secondary = Color(0xFF43A047), accent = Color(0xFF1B5E20), variantTag = "green_zucchini")
            }
        }
        if (lower.contains("pumpkin")) {
            return BotanicalPalette(primary = Color(0xFFFF9800), secondary = Color(0xFFFFB74D), accent = Color(0xFFE65100), variantTag = "pumpkin")
        }
        if (lower.contains("butternut") || lower.contains("spaghetti squash")) {
            return BotanicalPalette(primary = Color(0xFFFFCC80), secondary = Color(0xFFFFE0B2), accent = Color(0xFFFF9800), variantTag = "tan_squash")
        }
        if (lower.contains("kabocha") || lower.contains("acorn")) {
            return BotanicalPalette(primary = Color(0xFF1B5E20), secondary = Color(0xFF2E7D32), accent = Color(0xFFFF9800), variantTag = "kabocha")
        }

        // 18. Plums & Peaches & Stone Fruits
        if (lower.contains("plum") || lower.contains("prune") || lower.contains("pluot")) {
            return when {
                lower.contains("greengage") || lower.contains("green") ->
                    BotanicalPalette(primary = Color(0xFF9CCC65), secondary = Color(0xFFC5E1A5), accent = Color(0xFF689F38), variantTag = "green_plum")
                lower.contains("mirabelle") || lower.contains("yellow") ->
                    BotanicalPalette(primary = Color(0xFFFFCA28), secondary = Color(0xFFFFF59D), accent = Color(0xFFFF8F00), variantTag = "yellow_plum")
                else ->
                    BotanicalPalette(primary = Color(0xFF4A148C), secondary = Color(0xFF7B1FA2), accent = Color(0xFF311B92), variantTag = "purple_plum")
            }
        }
        if (lower.contains("peach") || lower.contains("nectarine")) {
            return when {
                lower.contains("white") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFF8E7),
                        secondary = Color(0xFFFFCDD2),
                        accent = Color(0xFFEF9A9A),
                        variantTag = "white_peach"
                    )
                lower.contains("donut") || lower.contains("saturn") ->
                    BotanicalPalette(
                        primary = Color(0xFFFFE082),
                        secondary = Color(0xFFFFD54F),
                        accent = Color(0xFFFF7043),
                        variantTag = "donut_peach"
                    )
                else ->
                    // Golden-amber Yellow Peach / Yellow Nectarine with warm sunset-red blush
                    BotanicalPalette(
                        primary = Color(0xFFFFB300),
                        secondary = Color(0xFFFFD54F),
                        accent = Color(0xFFE64A19),
                        variantTag = "yellow_peach"
                    )
            }
        }
        if (lower.contains("cherry")) {
            return if (lower.contains("rainier")) {
                BotanicalPalette(primary = Color(0xFFFFEE58), secondary = Color(0xFFFF8A80), accent = Color(0xFFE53935), variantTag = "rainier_cherry")
            } else {
                BotanicalPalette(primary = Color(0xFFB71C1C), secondary = Color(0xFFEF5350), accent = Color(0xFF880E4F), variantTag = "red_cherry")
            }
        }
        if (lower.contains("apricot")) {
            return BotanicalPalette(primary = Color(0xFFFFB74D), secondary = Color(0xFFFFD54F), accent = Color(0xFFFF8A65), variantTag = "apricot")
        }

        // 19. Tropicals
        if (lower.contains("mango")) {
            return BotanicalPalette(primary = Color(0xFFFFB300), secondary = Color(0xFFFFD54F), accent = Color(0xFFE53935), variantTag = "mango")
        }
        if (lower.contains("papaya")) {
            return BotanicalPalette(primary = Color(0xFFFF9800), secondary = Color(0xFFFF7043), accent = Color(0xFFE65100), variantTag = "papaya")
        }
        if (lower.contains("passion fruit") || lower.contains("passionfruit") || lower.contains("maracuja")) {
            return if (lower.contains("yellow") || lower.contains("maracuja")) {
                BotanicalPalette(primary = Color(0xFFFFD54F), secondary = Color(0xFFFFF176), accent = Color(0xFFFFB300), variantTag = "yellow_passionfruit")
            } else {
                BotanicalPalette(primary = Color(0xFF4A148C), secondary = Color(0xFF7B1FA2), accent = Color(0xFFFFB300), variantTag = "purple_passionfruit")
            }
        }
        if (lower.contains("guava")) {
            return BotanicalPalette(primary = Color(0xFFC5E1A5), secondary = Color(0xFFFF8A80), accent = Color(0xFF8BC34A), variantTag = "guava")
        }
        if (lower.contains("mangosteen")) {
            return BotanicalPalette(primary = Color(0xFF311B92), secondary = Color(0xFF512DA8), accent = Color(0xFFFAFAFA), variantTag = "mangosteen")
        }
        if (lower.contains("rambutan")) {
            return BotanicalPalette(primary = Color(0xFFD32F2F), secondary = Color(0xFFEF5350), accent = Color(0xFFC0CA33), variantTag = "rambutan")
        }
        if (lower.contains("durian")) {
            return BotanicalPalette(primary = Color(0xFF9E9D24), secondary = Color(0xFFC0CA33), accent = Color(0xFFFFD54F), variantTag = "durian")
        }
        if (lower.contains("jackfruit")) {
            return BotanicalPalette(primary = Color(0xFFAED581), secondary = Color(0xFFC5E1A5), accent = Color(0xFFFFD54F), variantTag = "jackfruit")
        }
        if (lower.contains("starfruit")) {
            return BotanicalPalette(primary = Color(0xFFFFEE58), secondary = Color(0xFFFFF9C4), accent = Color(0xFFC0CA33), variantTag = "starfruit")
        }
        if (lower.contains("coconut")) {
            return BotanicalPalette(primary = Color(0xFF6D4C41), secondary = Color(0xFF8D6E63), accent = Color(0xFF3E2723), variantTag = "coconut")
        }
        if (lower.contains("avocado")) {
            return BotanicalPalette(primary = Color(0xFF1E3821), secondary = Color(0xFF8BBF42), accent = Color(0xFFE9E598), variantTag = "avocado")
        }
        if (lower.contains("pomegranate")) {
            return BotanicalPalette(primary = Color(0xFFC2185B), secondary = Color(0xFFE91E63), accent = Color(0xFF880E4F), variantTag = "pomegranate")
        }
        if (lower.contains("fig")) {
            return if (lower.contains("kadota") || lower.contains("calimyrna") || lower.contains("green")) {
                BotanicalPalette(primary = Color(0xFFC5E1A5), secondary = Color(0xFFDCEDC8), accent = Color(0xFFFF8A80), variantTag = "green_fig")
            } else {
                BotanicalPalette(primary = Color(0xFF4A148C), secondary = Color(0xFF7B1FA2), accent = Color(0xFFAD1457), variantTag = "purple_fig")
            }
        }
        if (lower.contains("persimmon")) {
            return BotanicalPalette(primary = Color(0xFFFF6D00), secondary = Color(0xFFFF9800), accent = Color(0xFFE65100), variantTag = "persimmon")
        }

        // 20. Greens & Leaves & Herbs
        if (lower.contains("spinach") || lower.contains("kale") || lower.contains("chard") || lower.contains("lettuce") || lower.contains("arugula") || lower.contains("greens") || lower.contains("sorrel") || lower.contains("watercress") || lower.contains("herb") || lower.contains("basil") || lower.contains("mint") || lower.contains("parsley") || lower.contains("cilantro") || lower.contains("dill") || lower.contains("rosemary") || lower.contains("thyme") || lower.contains("oregano") || lower.contains("sage") || lower.contains("tarragon")) {
            return when {
                lower.contains("kale") || lower.contains("chard") || lower.contains("collard") ->
                    BotanicalPalette(primary = Color(0xFF2E7D32), secondary = Color(0xFF388E3C), accent = Color(0xFF1B5E20), variantTag = "dark_leafy")
                lower.contains("lettuce") || lower.contains("mâche") || lower.contains("endive") ->
                    BotanicalPalette(primary = Color(0xFFAED581), secondary = Color(0xFFC5E1A5), accent = Color(0xFF689F38), variantTag = "light_leafy")
                lower.contains("radicchio") ->
                    BotanicalPalette(primary = Color(0xFF880E4F), secondary = Color(0xFFAD1457), accent = Color(0xFFFAFAFA), variantTag = "radicchio")
                else ->
                    BotanicalPalette(primary = Color(0xFF388E3C), secondary = Color(0xFF4CAF50), accent = Color(0xFF1B5E20), variantTag = "green_leafy")
            }
        }

        // 21. Sea Vegetables
        if (lower.contains("nori") || lower.contains("wakame") || lower.contains("kombu") || lower.contains("seaweed") || archetype == "sea") {
            return BotanicalPalette(
                primary = Color(0xFF1B5E20),
                secondary = Color(0xFF2E7D32),
                accent = Color(0xFF0D2818),
                variantTag = "sea_vegetable"
            )
        }

        // 22. Archetype Fallback Defaults (Carefully Curated)
        return when (archetype) {
            "tropical" -> BotanicalPalette(primary = Color(0xFFFFB300), secondary = Color(0xFFFFD54F), accent = Color(0xFFE53935))
            "berry" -> BotanicalPalette(primary = Color(0xFFC2185B), secondary = Color(0xFFE91E63), accent = Color(0xFF880E4F))
            "root" -> BotanicalPalette(primary = Color(0xFFC69A64), secondary = Color(0xFFDDB98B), accent = Color(0xFF8D6E63))
            "stone" -> BotanicalPalette(primary = Color(0xFFFFB300), secondary = Color(0xFFFFD54F), accent = Color(0xFFE64A19))
            "exotic" -> BotanicalPalette(primary = Color(0xFFE91E63), secondary = Color(0xFFC2185B), accent = Color(0xFF8BC34A))
            "leafy", "herb" -> BotanicalPalette(primary = Color(0xFF388E3C), secondary = Color(0xFF4CAF50), accent = Color(0xFF1B5E20))
            "citrus" -> BotanicalPalette(primary = Color(0xFFFF9800), secondary = Color(0xFFFFB74D), accent = Color(0xFFE65100))
            "crucifer" -> BotanicalPalette(primary = Color(0xFF81C784), secondary = Color(0xFFA5D6A7), accent = Color(0xFF388E3C))
            "nightshade" -> BotanicalPalette(primary = Color(0xFFE53935), secondary = Color(0xFFEF5350), accent = Color(0xFFB71C1C))
            "gourd" -> BotanicalPalette(primary = Color(0xFFFF9800), secondary = Color(0xFFFFB74D), accent = Color(0xFFE65100))
            "pod" -> BotanicalPalette(primary = Color(0xFF689F38), secondary = Color(0xFF8BC34A), accent = Color(0xFF558B2F))
            "pome" -> BotanicalPalette(primary = Color(0xFFD63B2F), secondary = Color(0xFFFFB300), accent = Color(0xFFB71C1C))
            "allium" -> BotanicalPalette(primary = Color(0xFFE6C687), secondary = Color(0xFFF5DEB3), accent = Color(0xFFB8860B))
            "melon" -> BotanicalPalette(primary = Color(0xFFFFB74D), secondary = Color(0xFFFFCC80), accent = Color(0xFFF57F17))
            "vine" -> BotanicalPalette(primary = Color(0xFF6A1B9A), secondary = Color(0xFF9C27B0), accent = Color(0xFF4A148C))
            "stem" -> BotanicalPalette(primary = Color(0xFFAED581), secondary = Color(0xFFC5E1A5), accent = Color(0xFF558B2F))
            "mushroom" -> BotanicalPalette(primary = Color(0xFF8D6E63), secondary = Color(0xFFA1887F), accent = Color(0xFF5D4037))
            "sea" -> BotanicalPalette(primary = Color(0xFF1B5E20), secondary = Color(0xFF2E7D32), accent = Color(0xFF0D2818))
            else -> BotanicalPalette(primary = Color(0xFF4CAF50), secondary = Color(0xFF81C784), accent = Color(0xFF2E7D32))
        }
    }
}
