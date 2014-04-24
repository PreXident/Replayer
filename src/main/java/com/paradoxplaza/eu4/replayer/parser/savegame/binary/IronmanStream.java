package com.paradoxplaza.eu4.replayer.parser.savegame.binary;

import com.paradoxplaza.eu4.replayer.Date;
import static com.paradoxplaza.eu4.replayer.localization.Localizator.l10n;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

/**
 * Transforms binary eu4 save game stream to text eu4 save game stream.
 */
public class IronmanStream extends InputStream {

    /** Minimal size of PushbackInputStream. */
    static final int PUSH_BACK_BUFFER_SIZE = 4;

    /** Charset to use. Both input and output. */
    static final Charset charset = StandardCharsets.ISO_8859_1;

    /** Tokens occuring in EU4bin. */
    static final Map<Short, TokenInfo> tokens = new HashMap<>();

    /** Initialization of tokens. */
    static {
        final NumberProcessor numberProcessor = new NumberProcessor();
        final StringProcessor stringProcessor = new StringProcessor();
        final ValueIntProcessor valueIntprocessor = new ValueIntProcessor();
        tokens.put((short) 0x4D28, new TokenInfo("date", Output.QUOTED_DATE));
        tokens.put((short) 0x0100, new TokenInfo("="));
        tokens.put((short) 0x0C00, new TokenInfo(numberProcessor));
        tokens.put((short) 0x382A, new TokenInfo("player", Output.QUOTED_STRING));
        tokens.put((short) 0x0F00, new TokenInfo(stringProcessor));
        tokens.put((short) 0xC92E, new TokenInfo("savegame_version"));
        tokens.put((short) 0x0300, new TokenInfo("{"));
        tokens.put((short) 0x0400, new TokenInfo("}", new CloseBraceProcessor()));
        tokens.put((short) 0xE228, new TokenInfo("first", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xE328, new TokenInfo("second", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xC72E, new TokenInfo("third", Output.INT));
        tokens.put((short) 0xC82E, new TokenInfo("forth", Output.INT));
        tokens.put((short) 0xE12E, new TokenInfo("dlc_enabled", Output.QUOTED_STRING, true));
        tokens.put((short) 0x6E00, new TokenInfo("speed", Output.INT));
        tokens.put((short) 0xC62D, new TokenInfo("multiplayer_random_seed", Output.INT));
        tokens.put((short) 0x1400, new TokenInfo(numberProcessor));
        tokens.put((short) 0xC72D, new TokenInfo("multiplayer_random_count", Output.INT));
        tokens.put((short) 0x4028, new TokenInfo("monarch", Output.INT));
        tokens.put((short) 0xC829, new TokenInfo("cardinal", Output.INT));
        tokens.put((short) 0xBA28, new TokenInfo("leader", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xD928, new TokenInfo("advisor", Output.INT, false, valueIntprocessor));
        tokens.put((short) 0x3329, new TokenInfo("rebel", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xA628, new TokenInfo("unit", Output.INT));
        tokens.put((short) 0xCC29, new TokenInfo("flags", Output.STRING));
        tokens.put((short) 0x9B2C, new TokenInfo("gameplaysettings"));
        tokens.put((short) 0x992C, new TokenInfo("setgameplayoptions", Output.INT, true));
        tokens.put((short) 0xE428, new TokenInfo("start_date", Output.QUOTED_DATE));
        tokens.put((short) 0x7128, new TokenInfo("trade"));
        tokens.put((short) 0x5E01, new TokenInfo("node", new NodeProcessor()));
        tokens.put((short) 0x3528, new TokenInfo("definitions", Output.QUOTED_STRING));
        tokens.put((short) 0xAD2C, new TokenInfo("current", Output.DECIMAL));
        tokens.put((short) 0xA52D, new TokenInfo("local_value", Output.DECIMAL));
        tokens.put((short) 0xA82A, new TokenInfo("outgoing", Output.DECIMAL));
        tokens.put((short) 0x202B, new TokenInfo("value_added_outgoing", Output.DECIMAL));
        tokens.put((short) 0xA62D, new TokenInfo("retention", Output.DECIMAL));
        tokens.put((short) 0xA72D, new TokenInfo("steer_power", Output.DECIMAL));
        tokens.put((short) 0x152A, new TokenInfo("total", Output.INT, false, new TotalProcessor()));
        tokens.put((short) 0xEE2A, new TokenInfo("max", Output.DECIMAL));
        tokens.put((short) 0xFB2A, new TokenInfo("collector_power", Output.DECIMAL));
        tokens.put((short) 0xA82D, new TokenInfo("pull_power", Output.DECIMAL));
        tokens.put((short) 0xA92D, new TokenInfo("retain_power", Output.DECIMAL));
        tokens.put((short) 0xAA2D, new TokenInfo("highest_power", Output.DECIMAL));
        tokens.put((short) 0xB42A, new TokenInfo("power", Output.DECIMAL, false, new PowerProcessor()));
        tokens.put((short) 0x9D28, new TokenInfo("country", Output.QUOTED_STRING));
        tokens.put((short) 0xAB2D, new TokenInfo("max_power", Output.DECIMAL));
        tokens.put((short) 0xAC2D, new TokenInfo("province_power", Output.DECIMAL));
        tokens.put((short) 0x0B2B, new TokenInfo("ship_power", Output.DECIMAL));
        tokens.put((short) 0xAD2D, new TokenInfo("power_fraction", Output.DECIMAL));
        tokens.put((short) 0xAE2D, new TokenInfo("power_fraction_push", Output.DECIMAL));
        tokens.put((short) 0xAF2D, new TokenInfo("money", Output.DECIMAL));
        tokens.put((short) 0xE100, new TokenInfo("type", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xFC2A, new TokenInfo("actual_added_value", Output.DECIMAL));
        tokens.put((short) 0x092C, new TokenInfo("has_trader"));
        tokens.put((short) 0x0E00, new TokenInfo(new BooleanProcessor()));
        tokens.put((short) 0x942C, new TokenInfo("has_capital"));
        tokens.put((short) 0x232F, new TokenInfo("has_subject"));
        tokens.put((short) 0x062B, new TokenInfo("trade_goods_size", Output.DECIMAL, true));
        tokens.put((short) 0x0D00, new TokenInfo(new FloatProcessor()));
        tokens.put((short) 0x3C2B, new TokenInfo("top_provinces", Output.QUOTED_STRING, true));
        tokens.put((short) 0x3D2B, new TokenInfo("top_provinces_values", Output.DECIMAL, true));
        tokens.put((short) 0x3E2B, new TokenInfo("top_power", Output.QUOTED_STRING, true));
        tokens.put((short) 0x3F2B, new TokenInfo("top_power_values", Output.DECIMAL, true));
        tokens.put((short) 0xA72A, new TokenInfo("incoming"));
        tokens.put((short) 0xEE28, new TokenInfo("value", Output.DECIMAL, false, new ValueProcessor()));
        tokens.put((short) 0x9229, new TokenInfo("from", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xCF2E, new TokenInfo("production_leader_tag", Output.QUOTED_STRING, true));
        tokens.put((short) 0x6A2E, new TokenInfo("tradegoods_supply", Output.DECIMAL, true));
        tokens.put((short) 0x6B2E, new TokenInfo("tradegoods_demand", Output.DECIMAL, true));
        tokens.put((short) 0x6C2E, new TokenInfo("tradegoods_total_produced", Output.DECIMAL, true));
        tokens.put((short) 0x6D2E, new TokenInfo("tradegoods_num_supply_provinces", Output.INT, true));
        tokens.put((short) 0x6E2E, new TokenInfo("tradegoods_num_demand_provinces", Output.INT, true));
        tokens.put((short) 0x0B00, new TokenInfo("id", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xC72C, new TokenInfo("rebel_faction"));
        tokens.put((short) 0xF72A, new TokenInfo("fraction", Output.DECIMAL));
        tokens.put((short) 0x1B00, new TokenInfo("name", Output.QUOTED_STRING));
        tokens.put((short) 0xA92C, new TokenInfo("heretic", Output.QUOTED_STRING));
        tokens.put((short) 0xD22C, new TokenInfo("independence", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x3A28, new TokenInfo("culture", Output.QUOTED_STRING));
        tokens.put((short) 0x3E28, new TokenInfo("religion", Output.QUOTED_STRING));
        tokens.put((short) 0x6628, new TokenInfo("government", Output.QUOTED_STRING));
        tokens.put((short) 0x4328, new TokenInfo("province", Output.INT));
        tokens.put((short) 0x9A29, new TokenInfo("seed", Output.INT));
        tokens.put((short) 0xBE28, new TokenInfo("general"));
        tokens.put((short) 0xB928, new TokenInfo("manuever", Output.INT));
        tokens.put((short) 0xB728, new TokenInfo("fire", Output.INT));
        tokens.put((short) 0xB828, new TokenInfo("shock", Output.INT));
        tokens.put((short) 0xB628, new TokenInfo("siege", Output.INT));
        tokens.put((short) 0xC028, new TokenInfo("activation", Output.QUOTED_DATE));
        tokens.put((short) 0xA028, new TokenInfo("army"));
        tokens.put((short) 0x3328, new TokenInfo("provinces", Output.INT, true));
        tokens.put((short) 0x732A, new TokenInfo("friend", Output.STRING, true));
        tokens.put((short) 0xFC2D, new TokenInfo("possible_provinces", Output.INT, true));
        tokens.put((short) 0x822D, new TokenInfo("active"));
        tokens.put((short) 0x4C28, new TokenInfo("no"));
        tokens.put((short) 0x3929, new TokenInfo("emperor", Output.QUOTED_STRING));
        tokens.put((short) 0x9D2B, new TokenInfo("imperial_influence", Output.DECIMAL));
        tokens.put((short) 0x152B, new TokenInfo("reform_level", Output.INT));
        tokens.put((short) 0xAE27, new TokenInfo("imperial_ban_allowed"));
        tokens.put((short) 0xAD27, new TokenInfo("internal_hre_cb"));
        tokens.put((short) 0x172B, new TokenInfo("hre_inheritable"));
        tokens.put((short) 0x382C, new TokenInfo("allows_female_emperor"));
        tokens.put((short) 0x212D, new TokenInfo("old_emperor"));
        tokens.put((short) 0x4D2E, new TokenInfo("religions", Output.STRING));
        tokens.put((short) 0x1700, new TokenInfo(stringProcessor));
        tokens.put((short) 0xC729, new TokenInfo("papacy"));
        tokens.put((short) 0xA92E, new TokenInfo("papal_state", Output.QUOTED_STRING));
        tokens.put((short) 0x4228, new TokenInfo("controller", Output.QUOTED_STRING));
        tokens.put((short) 0xD72C, new TokenInfo("crusade_target", Output.QUOTED_STRING));
        tokens.put((short) 0xD82C, new TokenInfo("crusade_start", Output.QUOTED_DATE));
        tokens.put((short) 0xDA2C, new TokenInfo("last_excom", Output.QUOTED_DATE));
        tokens.put((short) 0xA72B, new TokenInfo("papacy_active"));
        tokens.put((short) 0xAA2E, new TokenInfo("weighted_cardinal", Output.INT));
        tokens.put((short) 0x0D2C, new TokenInfo("reform_desire", Output.DECIMAL));
        tokens.put((short) 0xC22D, new TokenInfo("active_cardinals"));
        tokens.put((short) 0xC42D, new TokenInfo("cardinal_age", Output.INT));
        tokens.put((short) 0x7028, new TokenInfo("location", Output.INT));
        tokens.put((short) 0xC52D, new TokenInfo("votes", Output.INT, true));
        tokens.put((short) 0xC32D, new TokenInfo("future_cardinals"));
        tokens.put((short) 0xFE2A, new TokenInfo("fired_events"));
        tokens.put((short) 0x632E, new TokenInfo("pending_events"));
        tokens.put((short) 0x4128, new TokenInfo("owner", Output.QUOTED_STRING));
        tokens.put((short) 0x5528, new TokenInfo("core", Output.QUOTED_STRING));
        tokens.put((short) 0x4E28, new TokenInfo("capital", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x932B, new TokenInfo("is_city"));
        tokens.put((short) 0x2B29, new TokenInfo("garrison", Output.DECIMAL));
        tokens.put((short) 0x3928, new TokenInfo("base_tax", Output.DECIMAL));
        tokens.put((short) 0x3F28, new TokenInfo("manpower", Output.DECIMAL));
        tokens.put((short) 0x4828, new TokenInfo("trade_goods", Output.STRING));
        tokens.put((short) 0x2D29, new TokenInfo("blockade"));
        tokens.put((short) 0x4B28, new TokenInfo("yes"));
        tokens.put((short) 0x3828, new TokenInfo("history"));
        tokens.put((short) 0x2327, new TokenInfo("add_core", Output.QUOTED_STRING));
        tokens.put((short) 0x4927, new TokenInfo("hre"));
        tokens.put((short) 0x4927, new TokenInfo("fort1"));
        tokens.put((short) 0x6A28, new TokenInfo("discovered_by", Output.QUOTED_STRING, false, new DiscoveredByProcessor()));
        tokens.put((short) 0x502A, new TokenInfo("revolt"));
        tokens.put((short) 0x2F00, new TokenInfo("size", Output.INT));
        tokens.put((short) 0x662A, new TokenInfo("patrol", Output.INT));
        tokens.put((short) 0x6928, new TokenInfo("discovery_dates", Output.QUOTED_DATE, true));
        tokens.put((short) 0x302A, new TokenInfo("discovery_religion_dates", Output.QUOTED_DATE, true));
        tokens.put((short) 0xD228, new TokenInfo("native_size", Output.DECIMAL));
        tokens.put((short) 0xD428, new TokenInfo("native_hostileness", Output.INT));
        tokens.put((short) 0xD328, new TokenInfo("native_ferocity", Output.INT));
        tokens.put((short) 0xA02D, new TokenInfo("trade_power", Output.DECIMAL));
        tokens.put((short) 0xD42E, new TokenInfo("hre_liberated"));
        tokens.put((short) 0x4F28, new TokenInfo("is_city"));
        tokens.put((short) 0xD828, new TokenInfo("skill", Output.INT));
        tokens.put((short) 0x1A2D, new TokenInfo("hire_date", Output.QUOTED_DATE));
        tokens.put((short) 0x9C29, new TokenInfo("death_date", Output.QUOTED_DATE));
        tokens.put((short) 0x222A, new TokenInfo("winter", Output.INT));
        tokens.put((short) 0xA62E, new TokenInfo("previous_winter", Output.INT));
        tokens.put((short) 0xA72E, new TokenInfo("has_winter_modifier"));
        tokens.put((short) 0x6829, new TokenInfo("modifier", Output.QUOTED_STRING));
        tokens.put((short) 0xE32D, new TokenInfo("ruler_modifier"));
        tokens.put((short) 0x0C2B, new TokenInfo("permanent"));
        tokens.put((short) 0x902D, new TokenInfo("hidden"));
        tokens.put((short) 0x7B2B, new TokenInfo("likely_rebels", Output.QUOTED_STRING));
        tokens.put((short) 0x2427, new TokenInfo("remove_core", Output.QUOTED_STRING));
        tokens.put((short) 0x8301, new TokenInfo("merchant_construction"));
        tokens.put((short) 0x082B, new TokenInfo("progress", Output.DECIMAL));
        tokens.put((short) 0xD62A, new TokenInfo("envoy", Output.INT, false, new EnvoyProcessor()));
        tokens.put((short) 0xA001, new TokenInfo("end"));
        tokens.put((short) 0x9329, new TokenInfo("to", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x4D00, new TokenInfo("direction", Output.INT));
        tokens.put((short) 0x5828, new TokenInfo("revolt_risk", Output.DECIMAL));
        tokens.put((short) 0x922B, new TokenInfo("colonysize", Output.DECIMAL));
        tokens.put((short) 0x432D, new TokenInfo("claim", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x5B2B, new TokenInfo("nationalism", Output.INT));
        tokens.put((short) 0x4E2E, new TokenInfo("countries"));
        tokens.put((short) 0x8B2A, new TokenInfo("hidden_flags"));
        tokens.put((short) 0x4D2A, new TokenInfo("variables"));
        tokens.put((short) 0x592F, new TokenInfo("initialized_rivals"));
        tokens.put((short) 0x2C2F, new TokenInfo("dirty_colony"));
        tokens.put((short) 0x142F, new TokenInfo("liberty_desire", Output.DECIMAL));
        tokens.put((short) 0x802B, new TokenInfo("unit_type", Output.STRING));
        tokens.put((short) 0x6501, new TokenInfo("none"));
        tokens.put((short) 0x6228, new TokenInfo("technology"));
        tokens.put((short) 0xF72C, new TokenInfo("rival", Output.QUOTED_STRING, false, new RivalProcessor()));
        tokens.put((short) 0xCB2E, new TokenInfo("rival_time", Output.INT));
        tokens.put((short) 0x092D, new TokenInfo("last_election", Output.QUOTED_DATE));
        tokens.put((short) 0x132B, new TokenInfo("diplomatic_power_cache", Output.INT));
        tokens.put((short) 0x102E, new TokenInfo("available_diplo_relations_cache", Output.INT));
        tokens.put((short) 0x762E, new TokenInfo("relations_over_limit", Output.INT));
        tokens.put((short) 0xD52E, new TokenInfo("delayed_treasure", Output.DECIMAL));
        tokens.put((short) 0x8129, new TokenInfo("num_of_trade_embargos", Output.INT));
        tokens.put((short) 0x292B, new TokenInfo("num_of_non_rival_trade_embargos", Output.INT));
        tokens.put((short) 0x2F2C, new TokenInfo("military_strength", Output.DECIMAL));
        tokens.put((short) 0x0329, new TokenInfo("land_morale", Output.DECIMAL));
        tokens.put((short) 0x0429, new TokenInfo("naval_morale", Output.DECIMAL));
        tokens.put((short) 0xC12E, new TokenInfo("max_land_morale", Output.DECIMAL));
        tokens.put((short) 0xC22E, new TokenInfo("max_naval_morale", Output.DECIMAL));
        tokens.put((short) 0x122F, new TokenInfo("tariff", Output.DECIMAL));
        tokens.put((short) 0x862E, new TokenInfo("total_war_worth", Output.INT));
        tokens.put((short) 0x7B29, new TokenInfo("num_of_revolts", Output.INT));
        tokens.put((short) 0x292F, new TokenInfo("num_owned_home_cores", Output.INT));
        tokens.put((short) 0x872E, new TokenInfo("num_of_controlled_cities", Output.INT));
        tokens.put((short) 0x7E29, new TokenInfo("num_of_ports", Output.INT));
        tokens.put((short) 0xD72E, new TokenInfo("num_of_non_cores", Output.INT));
        tokens.put((short) 0x882E, new TokenInfo("num_of_core_ports", Output.INT));
        tokens.put((short) 0x5C2B, new TokenInfo("num_of_total_ports", Output.INT));
        tokens.put((short) 0x592A, new TokenInfo("num_of_cardinals", Output.INT));
        tokens.put((short) 0x362C, new TokenInfo("num_of_mercenaries", Output.INT));
        tokens.put((short) 0x7D29, new TokenInfo("num_of_cities", Output.INT));
        tokens.put((short) 0xC42B, new TokenInfo("num_of_colonies", Output.INT));
        tokens.put((short) 0x892E, new TokenInfo("num_of_overseas", Output.INT));
        tokens.put((short) 0x8229, new TokenInfo("num_of_allies", Output.INT));
        tokens.put((short) 0x302F, new TokenInfo("num_of_independence_supporters", Output.INT));
        tokens.put((short) 0x8329, new TokenInfo("num_of_royal_marriages", Output.INT));
        tokens.put((short) 0x8A2E, new TokenInfo("num_of_throne_claims", Output.INT));
        tokens.put((short) 0x8429, new TokenInfo("num_of_vassals", Output.INT));
        tokens.put((short) 0x1C2F, new TokenInfo("num_of_protectorates", Output.INT));
        tokens.put((short) 0x082F, new TokenInfo("num_of_colonial_countries", Output.INT));
        tokens.put((short) 0x8529, new TokenInfo("num_of_unions", Output.INT));
        tokens.put((short) 0x8B2E, new TokenInfo("num_of_heathen_provs", Output.INT));
        tokens.put((short) 0x8C2E, new TokenInfo("num_of_heretic_provs", Output.INT));
        tokens.put((short) 0xA02B, new TokenInfo("num_of_missionaries", Output.INT));
        tokens.put((short) 0x8D2E, new TokenInfo("inland_sea_ratio", Output.DECIMAL));
        tokens.put((short) 0xBA2E, new TokenInfo("num_of_buildings", Output.INT, true));
        tokens.put((short) 0xBB2E, new TokenInfo("num_of_buildings_under_construction", Output.INT, true));
        tokens.put((short) 0xBC2E, new TokenInfo("produced_goods_value", Output.INT, true));
        tokens.put((short) 0x8E2E, new TokenInfo("num_of_goods_produced", Output.INT, true));
        tokens.put((short) 0x8F2E, new TokenInfo("num_of_religions", Output.INT, true));
        tokens.put((short) 0x902E, new TokenInfo("num_of_leaders", Output.INT, true));
        tokens.put((short) 0x912E, new TokenInfo("border_percentage", Output.DECIMAL, true));
        tokens.put((short) 0xD92E, new TokenInfo("border_size_in_tax", Output.DECIMAL, true));
        tokens.put((short) 0xDC2E, new TokenInfo("update_border_distance"));
        tokens.put((short) 0x482F, new TokenInfo("update_supply_range"));
        tokens.put((short) 0xD92E, new TokenInfo("border_size_in_tax", Output.DECIMAL, true));
        tokens.put((short) 0x292C, new TokenInfo("border_distance", Output.INT, true));
        tokens.put((short) 0xDA2E, new TokenInfo("border_distance_overseas", Output.INT, true));
        tokens.put((short) 0x4E00, new TokenInfo("range", Output.INT, true));
        tokens.put((short) 0x952E, new TokenInfo("is_neighbour", Output.INT, true));
        tokens.put((short) 0x962E, new TokenInfo("is_home_neighbour", Output.INT, true));
        tokens.put((short) 0x972E, new TokenInfo("is_core_neighbour", Output.INT, true));
        tokens.put((short) 0x272B, new TokenInfo("score", Output.DECIMAL));
        tokens.put((short) 0x3B2F, new TokenInfo("score_rating", Output.DECIMAL, true));
        tokens.put((short) 0x3C2F, new TokenInfo("score_rank", Output.INT, true));
        tokens.put((short) 0xA129, new TokenInfo("prestige", Output.DECIMAL));
        tokens.put((short) 0x8628, new TokenInfo("stability", Output.DECIMAL));
        tokens.put((short) 0x7628, new TokenInfo("treasury", Output.DECIMAL));
        tokens.put((short) 0x712E, new TokenInfo("call_for_peace", Output.DECIMAL));
        tokens.put((short) 0x3A2A, new TokenInfo("estimated_monthly_income", Output.DECIMAL));
        tokens.put((short) 0x422E, new TokenInfo("opinion_cache", Output.INT, true));
        tokens.put((short) 0xD82D, new TokenInfo("under_construction", Output.INT, true));
        tokens.put((short) 0x302B, new TokenInfo("under_construction_queued", Output.INT, true));
        tokens.put((short) 0xD92D, new TokenInfo("total_count", Output.INT, true));
        tokens.put((short) 0xA82E, new TokenInfo("idea_may_cache", Output.INT, true));
        tokens.put((short) 0x812E, new TokenInfo("update_opinion_cache"));
        tokens.put((short) 0x822E, new TokenInfo("needs_refresh"));
        tokens.put((short) 0x832E, new TokenInfo("needs_rebel_unit_refresh"));
        tokens.put((short) 0x842E, new TokenInfo("refresh_modifier"));
        tokens.put((short) 0xB428, new TokenInfo("last_bankrupt", Output.QUOTED_DATE));
        tokens.put((short) 0x0229, new TokenInfo("wartax", Output.QUOTED_DATE));
        tokens.put((short) 0x1629, new TokenInfo("land_maintenance", Output.DECIMAL));
        tokens.put((short) 0x1729, new TokenInfo("naval_maintenance", Output.DECIMAL));
        tokens.put((short) 0xFB2C, new TokenInfo("colonial_maintenance", Output.DECIMAL));
        tokens.put((short) 0x0E2D, new TokenInfo("missionary_maintenance", Output.DECIMAL));
        tokens.put((short) 0x5129, new TokenInfo("army_tradition", Output.DECIMAL));
        tokens.put((short) 0x5229, new TokenInfo("navy_tradition", Output.DECIMAL));
        tokens.put((short) 0x152E, new TokenInfo("military_rating", Output.INT));
        tokens.put((short) 0x4C2F, new TokenInfo("military_rating_cache", Output.INT));
        tokens.put((short) 0x0C29, new TokenInfo("naval_forcelimit", Output.DECIMAL));
        tokens.put((short) 0x5E2E, new TokenInfo("last_war_ended", Output.QUOTED_DATE));
        tokens.put((short) 0x082E, new TokenInfo("num_uncontested_cores", Output.INT));
        tokens.put((short) 0x7528, new TokenInfo("ledger"));
        tokens.put((short) 0x8827, new TokenInfo("loan_size", Output.INT));
        tokens.put((short) 0x0E2B, new TokenInfo("estimated_loan", Output.DECIMAL));
        tokens.put((short) 0xE72B, new TokenInfo("religious_unity", Output.DECIMAL));
        tokens.put((short) 0x512B, new TokenInfo("legitimacy", Output.DECIMAL));
        tokens.put((short) 0xFD2B, new TokenInfo("mercantilism", Output.DECIMAL));
        tokens.put((short) 0x602E, new TokenInfo("active_idea_groups"));
        tokens.put((short) 0x4527, new TokenInfo("colonists"));
        tokens.put((short) 0x4427, new TokenInfo("merchants"));
        tokens.put((short) 0x4327, new TokenInfo("missionaries"));
        tokens.put((short) 0x4627, new TokenInfo("diplomats"));
        tokens.put((short) 0x7329, new TokenInfo("max_manpower", Output.DECIMAL));
        tokens.put((short) 0xF22B, new TokenInfo("overextension_percentage", Output.DECIMAL));
        tokens.put((short) 0x612E, new TokenInfo("\n active_relations", Output.DECIMAL));
        tokens.put((short) 0xAF2E, new TokenInfo("has_colony_claim"));
        tokens.put((short) 0x912C, new TokenInfo("inauguration", Output.QUOTED_DATE));
        tokens.put((short) 0x002F, new TokenInfo("last_migration", Output.QUOTED_DATE));
        tokens.put((short) 0x052D, new TokenInfo("last_major_mission_pick", Output.QUOTED_DATE));
        tokens.put((short) 0x032D, new TokenInfo("last_major_mission_cancel", Output.QUOTED_DATE));
        tokens.put((short) 0x852B, new TokenInfo("last_major_mission", Output.QUOTED_STRING));
        tokens.put((short) 0x172E, new TokenInfo("last_strategy_index", Output.INT));
        tokens.put((short) 0xC32B, new TokenInfo("ai"));
        tokens.put((short) 0xF32C, new TokenInfo("initialized"));
        tokens.put((short) 0x142E, new TokenInfo("initialized_attitudes"));
        tokens.put((short) 0x0D01, new TokenInfo("static"));
        tokens.put((short) 0xFF2B, new TokenInfo("personality", Output.QUOTED_STRING));
        tokens.put((short) 0x312E, new TokenInfo("last_recalc_date", Output.QUOTED_DATE));
        tokens.put((short) 0xE72C, new TokenInfo("hre_interest"));
        tokens.put((short) 0xE82C, new TokenInfo("papacy_interest"));
        tokens.put((short) 0xB22A, new TokenInfo("powers", Output.INT, true));
        tokens.put((short) 0xA02A, new TokenInfo("traded_bonus", Output.INT, true));
        tokens.put((short) 0x6328, new TokenInfo("technology_group", Output.STRING));
        tokens.put((short) 0xFF2A, new TokenInfo("inflation_history", Output.DECIMAL, true));
        tokens.put((short) 0x7F28, new TokenInfo("income", Output.DECIMAL, true));
        tokens.put((short) 0x8028, new TokenInfo("expense", Output.DECIMAL, true));
        tokens.put((short) 0x7928, new TokenInfo("lastmonthincome", Output.DECIMAL));
        tokens.put((short) 0x7B28, new TokenInfo("lastmonthincometable", Output.DECIMAL, true));
        tokens.put((short) 0x7A28, new TokenInfo("lastmonthexpense", Output.DECIMAL));
        tokens.put((short) 0x7C28, new TokenInfo("lastmonthexpensetable", Output.DECIMAL, true));
        tokens.put((short) 0x3529, new TokenInfo("action", Output.QUOTED_DATE, false, new ActionProcessor()));
        tokens.put((short) 0x8127, new TokenInfo("infantry", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x8027, new TokenInfo("cavalry", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x8427, new TokenInfo("heavy_ship", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x8527, new TokenInfo("light_ship", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x8727, new TokenInfo("galley", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x8627, new TokenInfo("transport", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0xA728, new TokenInfo("movement_progress", Output.DECIMAL));
        tokens.put((short) 0xA228, new TokenInfo("regiment"));
        tokens.put((short) 0xA428, new TokenInfo("home", Output.INT));
        tokens.put((short) 0xA828, new TokenInfo("morale", Output.DECIMAL));
        tokens.put((short) 0xA928, new TokenInfo("strength", Output.DECIMAL));
        tokens.put((short) 0xEF28, new TokenInfo("graphical_culture", Output.QUOTED_STRING));
        tokens.put((short) 0x6B00, new TokenInfo("target", Output.INT));
        tokens.put((short) 0xF129, new TokenInfo("staging_province", Output.INT));
        tokens.put((short) 0x7401, new TokenInfo("path", Output.INT, true));
        tokens.put((short) 0x3B28, new TokenInfo("primary_culture", Output.STRING));
        tokens.put((short) 0x2927, new TokenInfo("add_accepted_culture", Output.STRING));
        tokens.put((short) 0xE028, new TokenInfo("union", Output.INT));
        tokens.put((short) 0xAA28, new TokenInfo("DIP", Output.INT));
        tokens.put((short) 0xAC28, new TokenInfo("ADM", Output.INT));
        tokens.put((short) 0xAB28, new TokenInfo("MIL", Output.INT));
        tokens.put((short) 0xB42B, new TokenInfo("dynasty", Output.QUOTED_STRING));
        tokens.put((short) 0x422D, new TokenInfo("birth_date", Output.QUOTED_DATE));
        tokens.put((short) 0x492F, new TokenInfo("nonhistoricaldynastyforhistoricalmonarch"));
        tokens.put((short) 0x3C28, new TokenInfo("accepted_culture", Output.STRING));
        tokens.put((short) 0x562A, new TokenInfo("is_subject"));
        tokens.put((short) 0xD32B, new TokenInfo("is_lesser_in_union"));
        tokens.put((short) 0xB62C, new TokenInfo("overlord", Output.QUOTED_STRING));
        tokens.put((short) 0x6529, new TokenInfo("luck"));
        tokens.put((short) 0x922E, new TokenInfo("neighbours", Output.QUOTED_STRING, true));
        tokens.put((short) 0x932E, new TokenInfo("home_neighbours", Output.QUOTED_STRING, true));
        tokens.put((short) 0x942E, new TokenInfo("core_neighbours", Output.QUOTED_STRING, true));
        tokens.put((short) 0x982E, new TokenInfo("friends", Output.QUOTED_STRING, true));
        tokens.put((short) 0x7D2E, new TokenInfo("owned_provinces", Output.INT, true));
        tokens.put((short) 0x7F2E, new TokenInfo("core_provinces", Output.INT, true));
        tokens.put((short) 0x9E2B, new TokenInfo("papal_influence", Output.DECIMAL));
        tokens.put((short) 0x0B2A, new TokenInfo("mercenary"));
        tokens.put((short) 0xA128, new TokenInfo("navy"));
        tokens.put((short) 0xA328, new TokenInfo("ship"));
        tokens.put((short) 0x3C2A, new TokenInfo("at_sea", Output.INT));
        tokens.put((short) 0x932C, new TokenInfo("current_route_target", Output.INT));
        tokens.put((short) 0xBA2D, new TokenInfo("last_at_sea", Output.QUOTED_DATE));
        tokens.put((short) 0xE22C, new TokenInfo("trust", Output.INT));
        tokens.put((short) 0x262C, new TokenInfo("attitude", Output.QUOTED_STRING));
        tokens.put((short) 0x132E, new TokenInfo("has_changed"));
        tokens.put((short) 0xAB2E, new TokenInfo("has_core_claim"));
        tokens.put((short) 0xAC2E, new TokenInfo("has_culture_group_claim"));
        tokens.put((short) 0xAD2E, new TokenInfo("has_religion_group_claim"));
        tokens.put((short) 0xEA2C, new TokenInfo("conquer_prov", valueIntprocessor));
        tokens.put((short) 0xEE2C, new TokenInfo("threat", valueIntprocessor));
        tokens.put((short) 0xF02C, new TokenInfo("antagonize", valueIntprocessor));
        tokens.put((short) 0xF02C, new TokenInfo("antagonize", valueIntprocessor));
        tokens.put((short) 0xF12C, new TokenInfo("befriend", valueIntprocessor));
        tokens.put((short) 0x162D, new TokenInfo("historical_friend", Output.QUOTED_STRING));
        tokens.put((short) 0xEA2A, new TokenInfo("enemy", Output.QUOTED_STRING));
        tokens.put((short) 0xB92E, new TokenInfo("rebel_threat", Output.INT));
        tokens.put((short) 0xDB2E, new TokenInfo("subjects", Output.QUOTED_STRING, true));
        tokens.put((short) 0x992E, new TokenInfo("vassals", Output.QUOTED_STRING, true));
        tokens.put((short) 0x9A2E, new TokenInfo("lesser_union_partners", Output.QUOTED_STRING, true));
        tokens.put((short) 0xA42E, new TokenInfo("relation_costing_power"));
        tokens.put((short) 0x2F2A, new TokenInfo("previous_monarch"));
        tokens.put((short) 0xEF2C, new TokenInfo("protect", valueIntprocessor));
        tokens.put((short) 0xE128, new TokenInfo("vassal", valueIntprocessor));
        tokens.put((short) 0x172D, new TokenInfo("historical_friends", Output.QUOTED_STRING, true));
        tokens.put((short) 0x052C, new TokenInfo("republican_tradition", Output.DECIMAL));
        tokens.put((short) 0x412D, new TokenInfo("heir"));
        tokens.put((short) 0x2B2E, new TokenInfo("succeeded"));
        tokens.put((short) 0x442D, new TokenInfo("monarch_name", Output.QUOTED_STRING));
        tokens.put((short) 0x222C, new TokenInfo("is_vassal"));
        tokens.put((short) 0x2B27, new TokenInfo("religion", Output.STRING));
        tokens.put((short) 0x182A, new TokenInfo("female"));
        tokens.put((short) 0x532B, new TokenInfo("is_at_war"));
        tokens.put((short) 0x9F2E, new TokenInfo("current_at_war_with", Output.QUOTED_STRING, true));
        tokens.put((short) 0x1927, new TokenInfo("war_exhaustion", Output.DECIMAL));
        tokens.put((short) 0xB82E, new TokenInfo("is_fighting_war_together", Output.INT));
        tokens.put((short) 0x4A29, new TokenInfo("last_war", Output.QUOTED_DATE));
        tokens.put((short) 0xA42A, new TokenInfo("opinion"));
        tokens.put((short) 0xCA2D, new TokenInfo("current_opinion", Output.DECIMAL));
        tokens.put((short) 0xB028, new TokenInfo("expiry_date", Output.QUOTED_DATE));
        tokens.put((short) 0x6F2E, new TokenInfo("delayed_decay"));
        tokens.put((short) 0x592E, new TokenInfo("cached_sum", Output.INT));
        tokens.put((short) 0xB02B, new TokenInfo("truce_with"));
        tokens.put((short) 0x182D, new TokenInfo("historical_rival", Output.QUOTED_STRING));
        tokens.put((short) 0x192D, new TokenInfo("historical_rivals", Output.QUOTED_STRING, true));
        tokens.put((short) 0x9D2E, new TokenInfo("guarantees", Output.QUOTED_STRING, true));
        tokens.put((short) 0x002A, new TokenInfo("regent"));
        tokens.put((short) 0xBD2C, new TokenInfo("decision", Output.QUOTED_STRING));
        tokens.put((short) 0xFC2B, new TokenInfo("piety", Output.DECIMAL));
        tokens.put((short) 0x9B2E, new TokenInfo("allies", Output.QUOTED_STRING, true));
        tokens.put((short) 0xAE2C, new TokenInfo("human"));
        tokens.put((short) 0x962D, new TokenInfo("interesting_countries", Output.INT, true));
        tokens.put((short) 0x8700, new TokenInfo("parent", Output.QUOTED_STRING));
        tokens.put((short) 0xA02E, new TokenInfo("current_war_allies", Output.QUOTED_STRING, true));
        tokens.put((short) 0x2A27, new TokenInfo("remove_accepted_culture", Output.STRING));
        tokens.put((short) 0x2C27, new TokenInfo("elector"));
        tokens.put((short) 0xAD2B, new TokenInfo("preferred_emperor", Output.QUOTED_STRING));
        tokens.put((short) 0x102D, new TokenInfo("last_hre_vote", Output.QUOTED_DATE));
        tokens.put((short) 0x4928, new TokenInfo("goldtype", Output.INT));
        tokens.put((short) 0xBB28, new TokenInfo("admiral"));
        tokens.put((short) 0xBC28, new TokenInfo("explorer"));
        tokens.put((short) 0xE92C, new TokenInfo("colonize_prov", valueIntprocessor));
        tokens.put((short) 0x1A2F, new TokenInfo("protectorate", valueIntprocessor));
        tokens.put((short) 0x032F, new TokenInfo("active_native_advancement"));
        tokens.put((short) 0x802A, new TokenInfo("faction", Output.STRING));
        tokens.put((short) 0x812A, new TokenInfo("influence", Output.DECIMAL));
        tokens.put((short) 0x882A, new TokenInfo("old_influence", Output.DECIMAL));
        tokens.put((short) 0x852E, new TokenInfo("top_faction", Output.INT));
        tokens.put((short) 0x802E, new TokenInfo("claim_provinces", Output.INT, true));
        tokens.put((short) 0xE92B, new TokenInfo("has_claim"));
        tokens.put((short) 0xFF2E, new TokenInfo("migrate", Output.INT));
        tokens.put((short) 0x262F, new TokenInfo("adjective", Output.QUOTED_STRING));
        tokens.put((short) 0x252F, new TokenInfo("map_color", Output.INT, true));
        tokens.put((short) 0x3D2F, new TokenInfo("country_color", Output.INT, true));
        tokens.put((short) 0xDA28, new TokenInfo("active_advisors"));
        tokens.put((short) 0xEC28, new TokenInfo("diplomacy"));
        tokens.put((short) 0xED28, new TokenInfo("casus_belli", Output.QUOTED_STRING));
        tokens.put((short) 0xE528, new TokenInfo("end_date", Output.QUOTED_DATE));
        tokens.put((short) 0xE928, new TokenInfo("cancel"));
        tokens.put((short) 0xDD28, new TokenInfo("alliance", valueIntprocessor));
        tokens.put((short) 0xDE28, new TokenInfo("guarantee"));
        tokens.put((short) 0xDF28, new TokenInfo("royal_marriage"));
        tokens.put((short) 0x1A29, new TokenInfo("combat\n"));
        tokens.put((short) 0xF728, new TokenInfo("active_war\n"));
        tokens.put((short) 0xE62D, new TokenInfo("war_goal"));
        tokens.put((short) 0x052A, new TokenInfo("tag", Output.QUOTED_STRING));
        tokens.put((short) 0xF028, new TokenInfo("add_attacker", Output.QUOTED_STRING));
        tokens.put((short) 0xF228, new TokenInfo("add_defender", Output.QUOTED_STRING));
        tokens.put((short) 0xF428, new TokenInfo("battle"));
        tokens.put((short) 0xFB28, new TokenInfo("result"));
        tokens.put((short) 0xF928, new TokenInfo("attacker", Output.QUOTED_STRING));
        tokens.put((short) 0xFC28, new TokenInfo("losses", Output.INT));
        tokens.put((short) 0x622D, new TokenInfo("commander", Output.QUOTED_STRING));
        tokens.put((short) 0xFA28, new TokenInfo("defender", Output.QUOTED_STRING));
        tokens.put((short) 0xF128, new TokenInfo("rem_attacker", Output.QUOTED_STRING));
        tokens.put((short) 0xF328, new TokenInfo("rem_defender", Output.QUOTED_STRING));
        tokens.put((short) 0x3E2D, new TokenInfo("original_attacker", Output.QUOTED_STRING));
        tokens.put((short) 0x3F2D, new TokenInfo("original_defender", Output.QUOTED_STRING));
        tokens.put((short) 0xE52D, new TokenInfo("defender_score", Output.DECIMAL));
        tokens.put((short) 0xEB2D, new TokenInfo("take_capital"));
        tokens.put((short) 0x242E, new TokenInfo("war_direction_quarter", Output.INT));
        tokens.put((short) 0x252E, new TokenInfo("war_direction_year", Output.INT));
        tokens.put((short) 0x262E, new TokenInfo("last_warscore_quarter", Output.INT));
        tokens.put((short) 0x272E, new TokenInfo("last_warscore_year", Output.INT));
        tokens.put((short) 0x282E, new TokenInfo("next_quarter_update", Output.QUOTED_DATE));
        tokens.put((short) 0x292E, new TokenInfo("next_year_update", Output.QUOTED_DATE));
        tokens.put((short) 0x3A2E, new TokenInfo("stalled_years", Output.INT));
        tokens.put((short) 0xE82D, new TokenInfo("take_core", Output.INT));
        tokens.put((short) 0xF828, new TokenInfo("previous_war\n"));
        tokens.put((short) 0xF82D, new TokenInfo("superiority"));
        tokens.put((short) 0xE92D, new TokenInfo("take_border"));
        tokens.put((short) 0xEA2D, new TokenInfo("take_province"));
        tokens.put((short) 0xFF29, new TokenInfo("succession", Output.STRING));
        tokens.put((short) 0x3701, new TokenInfo("income_statistics"));
        tokens.put((short) 0x3801, new TokenInfo("nation_size_statistics"));
        tokens.put((short) 0x2A2B, new TokenInfo("score_statistics"));
        tokens.put((short) 0x3901, new TokenInfo("inflation_statistics"));
        tokens.put((short) 0x7901, new TokenInfo("checksum", Output.QUOTED_STRING));
        tokens.put((short) 0xDC00, new TokenInfo("key", Output.QUOTED_STRING));
        tokens.put((short) 0x6D00, new TokenInfo("duration", Output.INT));
        tokens.put((short) 0x982A, new TokenInfo("production_leader", Output.INT, true));
        tokens.put((short) 0x052B, new TokenInfo("pirates"));
        tokens.put((short) 0x5C2D, new TokenInfo("defender_date", Output.QUOTED_DATE));
        tokens.put((short) 0x8B28, new TokenInfo("enable", Output.QUOTED_DATE));
        tokens.put((short) 0x5F2E, new TokenInfo("scope_is_valid"));
        tokens.put((short) 0x3F2E, new TokenInfo("province_modifiers"));
        tokens.put((short) 0x5A28, new TokenInfo("local_revolt_risk", Output.INT));
        tokens.put((short) 0xA22D, new TokenInfo("province_trade_power_value", Output.DECIMAL));
        tokens.put((short) 0x2C2B, new TokenInfo("province_trade_power_modifier", Output.DECIMAL));
        tokens.put((short) 0x5B2D, new TokenInfo("local_production_efficiency", Output.DECIMAL));
        tokens.put((short) 0x8528, new TokenInfo("tax_income", Output.DECIMAL));
        tokens.put((short) 0x8328, new TokenInfo("local_tax_modifier", Output.DECIMAL));
        tokens.put((short) 0x482B, new TokenInfo("local_missionary_strength", Output.DECIMAL));
        tokens.put((short) 0x0A29, new TokenInfo("local_manpower", Output.DECIMAL));
        tokens.put((short) 0x0829, new TokenInfo("local_manpower_modifier", Output.DECIMAL));
        tokens.put((short) 0x2729, new TokenInfo("attrition", Output.DECIMAL));
        tokens.put((short) 0x2829, new TokenInfo("max_attrition", Output.DECIMAL));
        tokens.put((short) 0x2929, new TokenInfo("supply_limit", Output.DECIMAL));
        tokens.put((short) 0x5D29, new TokenInfo("local_spy_defence", Output.DECIMAL));
        tokens.put((short) 0x6029, new TokenInfo("trade_value", Output.DECIMAL));
        tokens.put((short) 0x6329, new TokenInfo("fort_level", Output.DECIMAL));
        tokens.put((short) 0xA429, new TokenInfo("ship_recruit_speed", Output.DECIMAL));
        tokens.put((short) 0xA529, new TokenInfo("regiment_recruit_speed", Output.DECIMAL));
        tokens.put((short) 0x6129, new TokenInfo("trade_value_modifier", Output.DECIMAL));
        tokens.put((short) 0xFA29, new TokenInfo("garrison_growth", Output.DECIMAL));
        tokens.put((short) 0x5D2A, new TokenInfo("local_defensiveness", Output.DECIMAL));
        tokens.put((short) 0x7D2D, new TokenInfo("local_ship_repair", Output.DECIMAL));
        tokens.put((short) 0x7F2D, new TokenInfo("local_regiment_cost", Output.DECIMAL));
        tokens.put((short) 0x7E2D, new TokenInfo("local_movement_speed", Output.DECIMAL));
        tokens.put((short) 0xD52A, new TokenInfo("free_leader_pool", Output.DECIMAL));
        tokens.put((short) 0xF52D, new TokenInfo("missionary_progress", Output.DECIMAL));
        tokens.put((short) 0xC728, new TokenInfo("building_construction"));
        tokens.put((short) 0x5228, new TokenInfo("building", Output.INT));
        tokens.put((short) 0xD42A, new TokenInfo("enemy_core_creation", Output.DECIMAL));
        tokens.put((short) 0xC828, new TokenInfo("military_construction"));
        tokens.put((short) 0x5628, new TokenInfo("cost", Output.DECIMAL));
        tokens.put((short) 0x8728, new TokenInfo("stability_cost_modifier", Output.DECIMAL));
        tokens.put((short) 0x7C2D, new TokenInfo("local_ship_cost", Output.DECIMAL));
        tokens.put((short) 0x0B29, new TokenInfo("land_forcelimit", Output.DECIMAL));
        tokens.put((short) 0x8201, new TokenInfo("diplomacy_construction", new DiplomacyConstructionProcessor()));
        tokens.put((short) 0x3129, new TokenInfo("actor", Output.QUOTED_STRING));
        tokens.put((short) 0x3229, new TokenInfo("recipient", Output.QUOTED_STRING));
        tokens.put((short) 0xA42C, new TokenInfo("from_province", Output.INT));
        tokens.put((short) 0xA62C, new TokenInfo("to_province", Output.INT));
        tokens.put((short) 0x6D28, new TokenInfo("once"));
        tokens.put((short) 0x5928, new TokenInfo("minimum_revolt_risk", Output.DECIMAL));
        tokens.put((short) 0xC527, new TokenInfo("add_claim", Output.QUOTED_STRING));
        tokens.put((short) 0xC627, new TokenInfo("remove_claim", Output.QUOTED_STRING));
        tokens.put((short) 0x4B2A, new TokenInfo("local_colonial_growth", Output.QUOTED_STRING));
        tokens.put((short) 0xD02C, new TokenInfo("defection"));
        tokens.put((short) 0xDF2D, new TokenInfo("sow_discontent"));
        tokens.put((short) 0x4B27, new TokenInfo("discover"));
        tokens.put((short) 0xDD2D, new TokenInfo("sabotage_reputation"));
        tokens.put((short) 0xFD2D, new TokenInfo("build_core_construction"));
        tokens.put((short) 0xF12A, new TokenInfo("fabricate_claim"));
        tokens.put((short) 0x0D29, new TokenInfo("land_forcelimit_modifier", Output.DECIMAL));
        tokens.put((short) 0x5D2D, new TokenInfo("land_maintenance_modifier", Output.DECIMAL));
        tokens.put((short) 0x422A, new TokenInfo("technology_cost", Output.DECIMAL));
        tokens.put((short) 0x8928, new TokenInfo("inflation_reduction", Output.DECIMAL));
        tokens.put((short) 0xC928, new TokenInfo("missionary_construction"));
        tokens.put((short) 0xCD28, new TokenInfo("local_colonist_placement_chance", Output.DECIMAL));
        tokens.put((short) 0x672A, new TokenInfo("local_tariffs", Output.DECIMAL));
        tokens.put((short) 0x482E, new TokenInfo("possible_mercenary"));
        tokens.put((short) 0x7D28, new TokenInfo("lastyearincome", Output.DECIMAL, true));
        tokens.put((short) 0x7E28, new TokenInfo("lastyearexpense", Output.DECIMAL, true));
        tokens.put((short) 0xA629, new TokenInfo("previous", Output.INT));
        tokens.put((short) 0x922C, new TokenInfo("last_target"));
        tokens.put((short) 0x1D2D, new TokenInfo("ai_hard_strategy"));
        tokens.put((short) 0x3E2E, new TokenInfo("country_modifiers"));
        tokens.put((short) 0x5B28, new TokenInfo("global_revolt_risk", Output.DECIMAL));
        tokens.put((short) 0xF92A, new TokenInfo("global_prov_trade_power_modifier", Output.DECIMAL));
        tokens.put((short) 0xC328, new TokenInfo("global_trade_power", Output.DECIMAL));
        tokens.put((short) 0xCC28, new TokenInfo("colonist_placement_chance", Output.DECIMAL));
        tokens.put((short) 0x492B, new TokenInfo("global_missionary_strength", Output.DECIMAL));
        tokens.put((short) 0x0929, new TokenInfo("global_manpower", Output.DECIMAL));
        tokens.put((short) 0xCF2A, new TokenInfo("manpower_recovery_speed", Output.DECIMAL));
        tokens.put((short) 0x672D, new TokenInfo("merc_maintenance_modifier", Output.DECIMAL));
        tokens.put((short) 0x6C2A, new TokenInfo("mercenary_cost", Output.DECIMAL));
        tokens.put((short) 0x5329, new TokenInfo("army_tradition_decay", Output.DECIMAL));
        tokens.put((short) 0x5429, new TokenInfo("navy_tradition_decay", Output.DECIMAL));
        tokens.put((short) 0xBD29, new TokenInfo("prestige_decay", Output.DECIMAL));
        tokens.put((short) 0x1B2A, new TokenInfo("advisor_cost", Output.DECIMAL));
        tokens.put((short) 0x0D2B, new TokenInfo("advisor_pool", Output.DECIMAL));
        tokens.put((short) 0xBE2C, new TokenInfo("tolerance_own", Output.DECIMAL));
        tokens.put((short) 0xBF2C, new TokenInfo("tolerance_heretic", Output.DECIMAL));
        tokens.put((short) 0xC02C, new TokenInfo("tolerance_heathen", Output.DECIMAL));
        tokens.put((short) 0xF02D, new TokenInfo("recover_army_morale_speed", Output.DECIMAL));
        tokens.put((short) 0xE52A, new TokenInfo("diplomatic_upkeep", Output.DECIMAL));
        tokens.put((short) 0x422B, new TokenInfo("trade_steering", Output.DECIMAL));
        tokens.put((short) 0xF12D, new TokenInfo("recover_navy_morale_speed", Output.DECIMAL));
        tokens.put((short) 0x8227, new TokenInfo("artillery", Output.AMBIGUOUS_INT_QSTRING));
        tokens.put((short) 0x4B29, new TokenInfo("last_send_diplomat", Output.QUOTED_DATE));
        tokens.put((short) 0x5728, new TokenInfo("build_cost", Output.DECIMAL));
        tokens.put((short) 0x8428, new TokenInfo("global_tax_modifier", Output.DECIMAL));
        tokens.put((short) 0x0729, new TokenInfo("global_manpower_modifier", Output.DECIMAL));
        tokens.put((short) 0xC52A, new TokenInfo("infantry_power", Output.DECIMAL));
        tokens.put((short) 0x572A, new TokenInfo("defensiveness", Output.DECIMAL));
        tokens.put((short) 0x692A, new TokenInfo("diplomatic_reputation", Output.DECIMAL));
        tokens.put((short) 0x432B, new TokenInfo("fabricate_claims_time", Output.DECIMAL));
        tokens.put((short) 0xF32A, new TokenInfo("relations_decay_of_me", Output.DECIMAL));
        tokens.put((short) 0x042B, new TokenInfo("vassal_income", Output.DECIMAL));
        tokens.put((short) 0x3D2C, new TokenInfo("coalition_target", Output.QUOTED_STRING));
        tokens.put((short) 0x8A2D, new TokenInfo("base", Output.INT));
        tokens.put((short) 0xE32C, new TokenInfo("distrust", Output.INT));
        tokens.put((short) 0xEB2A, new TokenInfo("trade_prov", valueIntprocessor));
        tokens.put((short) 0x7328, new TokenInfo("trade_efficiency", Output.DECIMAL));
        tokens.put((short) 0x7428, new TokenInfo("production_efficiency", Output.DECIMAL));
        tokens.put((short) 0xAE28, new TokenInfo("interest", Output.DECIMAL));
        tokens.put((short) 0x0E29, new TokenInfo("naval_forcelimit_modifier", Output.DECIMAL));
        tokens.put((short) 0x5E2D, new TokenInfo("naval_maintenance_modifier", Output.DECIMAL));
        tokens.put((short) 0x1029, new TokenInfo("cavalry_cost", Output.DECIMAL));
        tokens.put((short) 0xC62A, new TokenInfo("cavalry_power", Output.DECIMAL));
        tokens.put((short) 0xCA2A, new TokenInfo("light_ship_power", Output.DECIMAL));
        tokens.put((short) 0xD22A, new TokenInfo("hostile_attrition", Output.DECIMAL));
        tokens.put((short) 0xA02C, new TokenInfo("land_attrition", Output.DECIMAL));
        tokens.put((short) 0x4C29, new TokenInfo("war_exhaustion_cost", Output.DECIMAL));
        tokens.put((short) 0x5529, new TokenInfo("leader_fire", Output.DECIMAL));
        tokens.put((short) 0x5629, new TokenInfo("leader_shock", Output.DECIMAL));
        tokens.put((short) 0x5729, new TokenInfo("leader_siege", Output.DECIMAL));
        tokens.put((short) 0x5C29, new TokenInfo("leader_naval_manuever", Output.DECIMAL));
        tokens.put((short) 0xCE2A, new TokenInfo("leader_land_manuever", Output.DECIMAL));
        tokens.put((short) 0x5E29, new TokenInfo("global_spy_defence", Output.DECIMAL));
        tokens.put((short) 0x5F29, new TokenInfo("spy_offence", Output.DECIMAL));
        tokens.put((short) 0x6429, new TokenInfo("blockade_efficiency", Output.DECIMAL));
        tokens.put((short) 0xBB29, new TokenInfo("prestige_from_land", Output.DECIMAL));
        tokens.put((short) 0xBC29, new TokenInfo("prestige_from_naval", Output.DECIMAL));
        tokens.put((short) 0x6229, new TokenInfo("global_trade_income_modifier", Output.DECIMAL));
        tokens.put((short) 0x982C, new TokenInfo("discipline", Output.DECIMAL));
        tokens.put((short) 0xB12C, new TokenInfo("reinforce_speed", Output.DECIMAL));
        tokens.put((short) 0x642A, new TokenInfo("global_ship_cost", Output.DECIMAL));
        tokens.put((short) 0x9E2A, new TokenInfo("trade_range_modifier", Output.DECIMAL));
        tokens.put((short) 0xD32A, new TokenInfo("core_creation", Output.DECIMAL));
        tokens.put((short) 0xFA2A, new TokenInfo("idea_cost", Output.DECIMAL));
        tokens.put((short) 0xDB2A, new TokenInfo("heir_chance", Output.DECIMAL));
        tokens.put((short) 0xCC2D, new TokenInfo("embargo_efficiency", Output.DECIMAL));
        tokens.put((short) 0xE62A, new TokenInfo("unjustified_demands", Output.DECIMAL));
        tokens.put((short) 0x442B, new TokenInfo("rebel_support_efficiency", Output.DECIMAL));
        tokens.put((short) 0x452B, new TokenInfo("discovered_relations_impact", Output.DECIMAL));
        tokens.put((short) 0x1429, new TokenInfo("light_ship_cost", Output.DECIMAL));
        tokens.put((short) 0x8828, new TokenInfo("inflation", Output.DECIMAL));
        tokens.put((short) 0x112E, new TokenInfo("exile_status"));
        tokens.put((short) 0x0F29, new TokenInfo("war_exhaustion", Output.DECIMAL));
        tokens.put((short) 0x1129, new TokenInfo("artillery_cost", Output.DECIMAL));
        tokens.put((short) 0x1329, new TokenInfo("heavy_ship_cost", Output.DECIMAL));
        tokens.put((short) 0x1529, new TokenInfo("galley_cost", Output.DECIMAL));
        tokens.put((short) 0xF629, new TokenInfo("transport_cost", Output.DECIMAL));
        tokens.put((short) 0xF629, new TokenInfo("transport_cost", Output.DECIMAL));
        tokens.put((short) 0x482A, new TokenInfo("global_ship_recruit_speed", Output.DECIMAL));
        tokens.put((short) 0x492A, new TokenInfo("prestige", Output.DECIMAL));
        tokens.put((short) 0x3629, new TokenInfo("trade_refusal"));
        tokens.put((short) 0xEB2B, new TokenInfo("patriarch_authority", Output.DECIMAL));
        tokens.put((short) 0x2629, new TokenInfo("convert"));
        tokens.put((short) 0xAD28, new TokenInfo("loan"));
        tokens.put((short) 0xAF28, new TokenInfo("lender", Output.QUOTED_STRING));
        tokens.put((short) 0xA101, new TokenInfo("amount", Output.INT));
        tokens.put((short) 0xD52B, new TokenInfo("active_major_mission"));
        tokens.put((short) 0x9929, new TokenInfo("scope"));
        tokens.put((short) 0x9A2D, new TokenInfo("from_scope"));
        tokens.put((short) 0x9D2D, new TokenInfo("root_scope"));
        tokens.put((short) 0x9C2D, new TokenInfo("prev_scope"));
        tokens.put((short) 0xC528, new TokenInfo("global_own_trade_power", Output.DECIMAL));
        tokens.put((short) 0xE22D, new TokenInfo("coalition_against_us", Output.QUOTED_STRING));
        tokens.put((short) 0xD82A, new TokenInfo("force_march"));
        tokens.put((short) 0xAB2A, new TokenInfo("attachments"));
        tokens.put((short) 0x2429, new TokenInfo("retreat"));
        tokens.put((short) 0xEF2D, new TokenInfo("retreat_shattered"));
        tokens.put((short) 0x3729, new TokenInfo("military_access", valueIntprocessor));
        tokens.put((short) 0xC428, new TokenInfo("global_foreign_trade_power", Output.DECIMAL));
        tokens.put((short) 0xC72A, new TokenInfo("artillery_power", Output.DECIMAL));
        tokens.put((short) 0xC92A, new TokenInfo("heavy_ship_power", Output.DECIMAL));
        tokens.put((short) 0x5029, new TokenInfo("overseas_income", Output.DECIMAL));
        tokens.put((short) 0x1F2D, new TokenInfo("changed_tag_from", Output.QUOTED_STRING));
        tokens.put((short) 0xA12C, new TokenInfo("naval_attrition", Output.DECIMAL));
        tokens.put((short) 0x4A2A, new TokenInfo("global_colonial_growth", Output.DECIMAL));
        tokens.put((short) 0x682A, new TokenInfo("global_tariffs", Output.DECIMAL));
        tokens.put((short) 0x402B, new TokenInfo("colonist_time", Output.DECIMAL));
        tokens.put((short) 0xDB2B, new TokenInfo("is_colonial"));
        tokens.put((short) 0xEC2C, new TokenInfo("building_prov", valueIntprocessor));
        tokens.put((short) 0x1D2B, new TokenInfo("westernisation", Output.DECIMAL));
        tokens.put((short) 0x652A, new TokenInfo("global_regiment_cost", Output.DECIMAL));
        tokens.put((short) 0x242B, new TokenInfo("global_garrison_growth", Output.DECIMAL));
        tokens.put((short) 0xBD28, new TokenInfo("conquistador"));
        tokens.put((short) 0xE82A, new TokenInfo("possible_mercenaries", Output.DECIMAL));
        tokens.put((short) 0xDC2A, new TokenInfo("imperial_authority", Output.DECIMAL));
        tokens.put((short) 0xCB2A, new TokenInfo("galley_power", Output.DECIMAL));
        tokens.put((short) 0x8128, new TokenInfo("thismonthincome", Output.DECIMAL, true));
        tokens.put((short) 0xDB2D, new TokenInfo("improve_relation"));
        tokens.put((short) 0x022A, new TokenInfo("warning"));
        tokens.put((short) 0x5C2E, new TokenInfo("annul_treaties"));
        tokens.put((short) 0xD62D, new TokenInfo("fleet_access"));
        tokens.put((short) 0x1F29, new TokenInfo("\nsiege_combat"));
        tokens.put((short) 0x1B29, new TokenInfo("phase", Output.INT));
        tokens.put((short) 0x7C00, new TokenInfo("day", Output.INT));
        tokens.put((short) 0x1C29, new TokenInfo("dice", Output.INT));
        tokens.put((short) 0xD42D, new TokenInfo("is_attacker"));
        tokens.put((short) 0xFD28, new TokenInfo("losses_type", Output.DECIMAL, true));
        tokens.put((short) 0x642E, new TokenInfo("participating_country", Output.QUOTED_STRING));
        tokens.put((short) 0xE329, new TokenInfo("front"));
        tokens.put((short) 0xE429, new TokenInfo("back"));
        tokens.put((short) 0x5C28, new TokenInfo("terrain", Output.QUOTED_STRING));
        tokens.put((short) 0x2A29, new TokenInfo("breach", Output.INT));
        tokens.put((short) 0xB101, new TokenInfo("roll", Output.INT));
        tokens.put((short) 0x302E, new TokenInfo("last_assault", Output.QUOTED_DATE));
        tokens.put((short) 0x1D29, new TokenInfo("\nland_combat"));
        tokens.put((short) 0xE42D, new TokenInfo("attacker_score", Output.DECIMAL));
        tokens.put((short) 0xDF29, new TokenInfo("demand", Output.QUOTED_STRING));
        tokens.put((short) 0xAB2C, new TokenInfo("revolutionary_war"));
        tokens.put((short) 0xE72D, new TokenInfo("take_colony"));
        tokens.put((short) 0x4F29, new TokenInfo("continent", Output.QUOTED_STRING));
        tokens.put((short) 0xEC2D, new TokenInfo("defend_capital"));
        tokens.put((short) 0xED2D, new TokenInfo("defend_country"));
        tokens.put((short) 0x3401, new TokenInfo("ledger_data"));
        tokens.put((short) 0x3601, new TokenInfo("ledger_data_y", Output.INT, true));
        tokens.put((short) 0x3501, new TokenInfo("ledger_data_x", Output.INT, true));
        tokens.put((short) 0x102F, new TokenInfo("random_world", Output.INT, true));
        tokens.put((short) 0x112F, new TokenInfo("used_colonial_names", Output.QUOTED_STRING, true));
        tokens.put((short) 0xAC2C, new TokenInfo("revolution_target", Output.QUOTED_STRING, true));
        tokens.put((short) 0x0A2F, new TokenInfo("dynamic_countries", Output.QUOTED_STRING, true));
        tokens.put((short) 0xBD2E, new TokenInfo("latest_pirate_targets", Output.INT, true));
        tokens.put((short) 0xBB27, new TokenInfo("random", Output.INT));
        tokens.put((short) 0x382F, new TokenInfo("native_size_before_migration", Output.DECIMAL));
        tokens.put((short) 0xD62E, new TokenInfo("action_token", Output.STRING));
        tokens.put((short) 0x4329, new TokenInfo("milaccess"));
        tokens.put((short) 0x3E29, new TokenInfo("embargoaction"));
        tokens.put((short) 0x682E, new TokenInfo("original_coloniser", Output.QUOTED_STRING));
        tokens.put((short) 0x3F29, new TokenInfo("annexationaction"));
        tokens.put((short) 0xDA2D, new TokenInfo("enforce_peace"));
        tokens.put((short) 0xA12E, new TokenInfo("trade_embargoed_by", Output.QUOTED_STRING, true));
        tokens.put((short) 0x462F, new TokenInfo("can_attach"));
        tokens.put((short) 0x662E, new TokenInfo("main_army"));
        tokens.put((short) 0xF229, new TokenInfo("invasion"));
        tokens.put((short) 0xA52E, new TokenInfo("recalc_attitude"));
        tokens.put((short) 0x872D, new TokenInfo("original", Output.QUOTED_STRING));
        tokens.put((short) 0x2E2A, new TokenInfo("last_command_date", Output.QUOTED_DATE));
        tokens.put((short) 0x9E2E, new TokenInfo("warnings", Output.QUOTED_STRING, true));
        tokens.put((short) 0x8228, new TokenInfo("thismonthexpense", Output.DECIMAL, true));
        tokens.put((short) 0x1D2F, new TokenInfo("protectorates", Output.QUOTED_STRING, true));
        tokens.put((short) 0xA32E, new TokenInfo("transfer_trade_power_from", Output.QUOTED_STRING, true));
        tokens.put((short) 0x672E, new TokenInfo("hunter_killer"));
        tokens.put((short) 0x072F, new TokenInfo("colonies", Output.QUOTED_STRING, true));
        tokens.put((short) 0xB52E, new TokenInfo("is_sabotaging_reputation"));
        tokens.put((short) 0xB42E, new TokenInfo("is_sowing_discontent"));
        tokens.put((short) 0xB12E, new TokenInfo("fabricating_claim_province", Output.INT));
        tokens.put((short) 0xB22E, new TokenInfo("is_fabricating_claim"));
        tokens.put((short) 0x552F, new TokenInfo("ignore_decision", Output.QUOTED_STRING));
        tokens.put((short) 0xBE2E, new TokenInfo("needs_help"));
        tokens.put((short) 0xCA2E, new TokenInfo("coalition_date", Output.QUOTED_DATE));
        tokens.put((short) 0x1B2F, new TokenInfo("is_protectorate"));
        tokens.put((short) 0xA22E, new TokenInfo("transfer_trade_power_to", Output.QUOTED_STRING, true));
        tokens.put((short) 0x9C2E, new TokenInfo("coalition_friends", Output.QUOTED_STRING, true));
        tokens.put((short) 0x3A2F, new TokenInfo("westernisation_needed", Output.INT));
        tokens.put((short) 0x092F, new TokenInfo("is_colonial_subject"));
        tokens.put((short) 0xCB2D, new TokenInfo("delayed_event"));
        tokens.put((short) 0xB801, new TokenInfo("event", Output.QUOTED_STRING));
        tokens.put((short) 0x7029, new TokenInfo("days", Output.INT));
        tokens.put((short) 0xCD2D, new TokenInfo("annexation"));
        tokens.put((short) 0x2A2A, new TokenInfo("subsidies"));
        tokens.put((short) 0x582F, new TokenInfo("inactive"));
        tokens.put((short) 0xE529, new TokenInfo("reserves"));
        tokens.put((short) 0x1E29, new TokenInfo("\nnaval_combat"));
        tokens.put((short) 0xE829, new TokenInfo("positioning", Output.DECIMAL));
        tokens.put((short) 0xDE29, new TokenInfo("terms"));
        tokens.put((short) 0xE929, new TokenInfo("annex"));
        tokens.put((short) 0xFD29, new TokenInfo("better"));
        tokens.put((short) 0x362D, new TokenInfo("concede"));
        tokens.put((short) 0xC127, new TokenInfo("revoke_reform"));
        tokens.put((short) 0x052E, new TokenInfo("revoke_elector"));
        tokens.put((short) 0x212F, new TokenInfo("humiliate"));
        tokens.put((short) 0x182E, new TokenInfo("file_name", Output.QUOTED_STRING)); //TODO ???
        tokens.put((short) 0xFE2D, new TokenInfo("change_culture_construction"));
        tokens.put((short) 0x4D29, new TokenInfo("callaction"));
        tokens.put((short) 0x4E29, new TokenInfo("versus", Output.QUOTED_STRING));
        tokens.put((short) 0x3F2A, new TokenInfo("replace"));
        tokens.put((short) 0x3D29, new TokenInfo("allianceaction"));
        tokens.put((short) 0x092E, new TokenInfo("transfer_trade_power"));
        tokens.put((short) 0x8D2A, new TokenInfo("finish", Output.INT, true)); //TODO ???
        tokens.put((short) 0xA227, new TokenInfo("kill_heir"));
        tokens.put((short) 0xBF2E, new TokenInfo("war_target", Output.QUOTED_STRING));
        tokens.put((short) 0xC02E, new TokenInfo("naval_war"));
        tokens.put((short) 0x472F, new TokenInfo("opportunism"));
        tokens.put((short) 0xD32E, new TokenInfo("war_date", Output.QUOTED_DATE));
        tokens.put((short) 0x9C2A, new TokenInfo("integrationaction"));
        tokens.put((short) 0x3B29, new TokenInfo("declarewar"));
        tokens.put((short) 0x4229, new TokenInfo("warningaction"));
        tokens.put((short) 0xC628, new TokenInfo("colony_construction"));
        tokens.put((short) 0xDE2E, new TokenInfo("MYUNKNOWN")); //castile.eu4
        tokens.put((short) 0xD52D, new TokenInfo("integration"));
    }

    /**
     * Reads count bytes from is.
     * @param is input stream to read from
     * @param count read this number of bytes
     * @return read bytes
     * @throws IOException when IOException occurs during reading
     * or not enough bytes are read
     */
    static private byte[] readBytes(final InputStream is, final int count)
            throws IOException {
        final byte[] bytes = new byte[count];
        readBytes(is, bytes);
        return bytes;
    }

    /**
     * Reads count bytes from is.
     * @param is input stream to read from
     * @param out array to store read bytes
     * @throws IOException when IOException occurs during reading
     * or not enough bytes are read
     */
    static private void readBytes(final InputStream is, final byte[] out)
            throws IOException {
        if (is.read(out) != out.length) {
            throw new IOException(l10n("parser.eof.unexpected"));
        }
    }

    /**
     * Indicator what is about to be read now.
     */
    enum State {
        /** Read the header "EU4bin". */
        START,
        /** Read next token. */
        TOKEN,
        /** Nothing to read, return -1. */
        END
    }

    /**
     * Context for ITokenProcessors.
     */
    enum Output {
        /** Output date. */
        DATE,
        /** Output quoted date. */
        QUOTED_DATE,
        /** Output string. */
        STRING,
        /** Output quoted string. */
        QUOTED_STRING,
        /** Output integer. */
        INT,
        /** Output decimal number with 3 decimal places. */
        DECIMAL,
        /** AMBIGUOUS can be followed by INT or QUOTED_STRING. */
        AMBIGUOUS_INT_QSTRING,
        /** AMBIGUOUS can be followed by DECIMAL or QUOTED_STRING. */
        AMBIGUOUS_DECIMAL_QSTRING,
        /** Indicator to clear the output to null. */
        NONE
    }

    /**
     * Context of the save game.
     */
    enum Context {
        /** Content of action token should be int. */
        ACTION_INT,
        /** Content of action token should be quoted string. */
        ACTION_STRING,
        /** Content of total token should be decimal. */
        TOTAL_DECIMAL,
        /** Content of value token should be int. */
        VALUE_INT
    }

    /** Underlaying input stream with binary save. */
    final PushbackInputStream in;

    /** Context of the save game. */
    final LinkedList<Context> context = new LinkedList<>();

    /** Accumulator of the output. */
    final StringBuilder builder = new StringBuilder();

    /** Output buffer.
     * Strings to be sent up are stored here.
     */
    byte[] buff = new byte[0];

    /** Next byte should be return from buff on this position. */
    int bufPos = 0;

    /** Indicator what is about to be read now. */
    State state = State.START;

    /** Context for ITokenProcessors. */
    Output output = null;

    /** Flag indicating whether list of values is being processed. */
    boolean inList = false;

    /**
     * Creates IronmanStream from underlaying PushbackInputStream.
     * It's buffer's size must be at least {@link #PUSH_BACK_BUFFER_SIZE}.
     * @param in EU4bin stream to be converted
     */
    public IronmanStream(final PushbackInputStream in) {
        this.in = in;
    }

    /**
     * Creates IronmanStream from ordinary stream.
     * @param in EU4bin stream to be converted
     */
    public IronmanStream(final InputStream in) {
        this.in = new PushbackInputStream(in, PUSH_BACK_BUFFER_SIZE);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        if (bufPos < buff.length) {
            return buff[bufPos++];
        }
        switch (state) {
            case START:
                readStart();
                break;
            case TOKEN:
                readToken();
                break;
            case END:
                return -1;
        }
        return state == State.END ? -1 : buff[bufPos++];
    }

    /**
     * Reads header.
     * @throws IOException if something goes wrong
     */
    private void readStart() throws IOException {
        final byte[] header = new byte[6];
        final int count = in.read(header);
        if (count != 6 || !new String(header, charset).equals("EU4bin")) {
            throw new IOException(l10n("parser.binary.notEU4"));
        }
        buff = "EU4txt ".getBytes(charset);
        bufPos = 0;
        state = State.TOKEN;
    }

    /**
     * Reads token, consults tokens hashmap and behaves accordingly.
     * Updates buff, bufPos, output and state.
     * @throws IOException when something goes wrong
     */
    private void readToken() throws IOException {
        final int b1 = in.read();
        if (b1 == -1) {
            state = State.END;
            return;
        }
        final int b2 = in.read();
        if (b2 == -1) {
            state = State.END;
            return;
        }
        final short token = (short) ((b1 << 8) + b2);
        final TokenInfo info = tokens.get(token);
        if (info == null) {
            throw new IOException(String.format(
                    l10n("parser.binary.token.unknown"), token & 0xFFFF));
        }
        if (info.output != null) {
            output = info.output == Output.NONE ? null : info.output;
        }
        if (info.list) {
            inList = true;
        }
        builder.setLength(0);
        builder.append(info.text);
        if (info.processor != null) {
            info.processor.processToken(this, builder);
        }
        builder.append(' ');
        buff = builder.toString().getBytes(charset);
        bufPos = 0;
    }

    /**
     * Simple interface called when specific token should be processed.
     */
    interface ITokenProcessor {

        /**
         * Reads is.in and appends to builder.
         * @param is calling IronmanStream
         * @param builder EU4txt output
         * @throws IOException when something goes wrong
         */
        void processToken(IronmanStream is, StringBuilder builder) throws IOException;
    }

    /**
     * Processes action keyword.
     */
    static class ActionProcessor implements ITokenProcessor {
        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            if (is.context.contains(Context.ACTION_INT)) {
                is.output = Output.INT;
            } else if (is.context.contains(Context.ACTION_STRING)) {
                is.output = Output.QUOTED_STRING;
            }
        }
    }

    /**
     * Processes booleans.
     */
    static class BooleanProcessor extends SingleValueProcessor {

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            final int flag = is.in.read();
            if (flag == -1) {
                throw new IOException(l10n("parser.eof.unexpected"));
            }
            builder.append(flag > 0 ? "yes" : "no");
        }
    }

    /**
     * Processes closing brace }.
     */
    static class CloseBraceProcessor implements ITokenProcessor {
        @Override
        public void processToken(IronmanStream is, StringBuilder builder) throws IOException {
            is.inList = false;
            is.output = null;
            if (!is.context.isEmpty()) {
                is.context.pop();
            }
        }
    }

    /**
     * Processes diplomacy_construction token.
     */
    static class DiplomacyConstructionProcessor implements ITokenProcessor {
        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            is.context.push(Context.ACTION_STRING);
        }
    }

    /**
     * Processes discovered_by token.
     */
    static class DiscoveredByProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.inList = true;
                is.output = Output.STRING;
            }
            is.in.unread(bytes);
        }
    }

    /**
     * Processes closing brace }.
     */
    static class EnvoyProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(IronmanStream is, StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.context.push(Context.ACTION_INT);
            }
            is.in.unread(bytes);
        }
    }

    /**
     * Processes floats.
     */
    static class FloatProcessor extends SingleValueProcessor {

        /** Here the float bytes will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            for(int i = 0, o = bytes.length -1; i < bytes.length / 2; ++i, --o) {
                final byte swap = bytes[i];
                bytes[i] = bytes[o];
                bytes[o] = swap;
            }
            final ByteBuffer bb = ByteBuffer.wrap(bytes);
            final float f = bb.getFloat();
            builder.append(String.format(Locale.ENGLISH, "%.3f", f));
        }
    }

    /**
     * Processes node token.
     */
    static class NodeProcessor implements ITokenProcessor {
        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            is.context.push(Context.TOTAL_DECIMAL);
        }
    }

    /**
     * Processes numbers and numeric dates.
     */
    static class NumberProcessor extends SingleValueProcessor {

        /** Numeric dates are represented as number of hours since this date. */
        static final Date start = new Date((short) -5000, (byte) 1, (byte) 1);

        /** Here the number bytes will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final int number = toNumber(bytes);
            if (is.output == null) {
                //throw new IllegalStateException(l10n("parser.binary.output.none"));
                builder.append(number);
                return;
            }
            switch (is.output) {
                case DATE:
                    final Date date = start.skip(Date.Period.DAY, number / 24);
                    builder.append(date);
                    break;
                case QUOTED_DATE:
                    final Date qdate = start.skip(Date.Period.DAY, number / 24);
                    builder.append('"');
                    builder.append(qdate);
                    builder.append('"');
                    break;
                case INT:
                case AMBIGUOUS_INT_QSTRING:
                    builder.append(number);
                    break;
                case DECIMAL:
                case AMBIGUOUS_DECIMAL_QSTRING:
                    float f = number / 1000f;
                    builder.append(String.format(Locale.ENGLISH, "%.3f", f));
                    break;
                default:
                    throw new IllegalStateException(String.format(
                            l10n("parser.binary.output.invalid"), is.output));
            }
        }
    }

    /**
     * Processes power token.
     */
    static class PowerProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.context.push(Context.TOTAL_DECIMAL);
            }
            is.in.unread(bytes);
        }
    }


    /**
     * Processes discovered_by token.
     */
    static class RivalProcessor implements ITokenProcessor {

        /** Here the next two tokens will be read. */
        final byte[] bytes = new byte[4];

        @Override
        public void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, bytes);
            final short token1 = (short) ((bytes[0] << 8) + bytes[1]);
            final short token2 = (short) ((bytes[2] << 8) + bytes[3]);
            //does the list follow?
            if (token1 == (short) 0x0100 /*=*/
                    && token2 == (short) 0x0300 /*{*/) {
                is.context.push(Context.VALUE_INT);
            }
            is.in.unread(bytes);
        }
    }

    /**
     * Common ancestor for Processors of values.
     * If not in a list, clears the is.output.
     */
    static abstract class SingleValueProcessor implements ITokenProcessor {

        /**
         * Converts byte array to integer it represents.
         * @param bytes convert these bytes
         * @return converted integer
         */
        static protected int toNumber(final byte[] bytes) {
            int number = 0;
            for(int i = bytes.length - 1; i >= 0; --i) {
                number <<= 8;
                number += bytes[i] & 0xff;
            }
            return number;
        }

        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            processValue(is, builder);
            if (!is.inList) {
                is.output = null;
            }
        }

        /**
         * Processes the value.
         * @param is calling IronmanStream
         * @param builder EU4txt output
         * @throws IOException when something goes wrong
         */
        protected abstract void processValue(IronmanStream is,
                StringBuilder builder) throws IOException;
    }

    /**
     * Processes strings.
     */
    static class StringProcessor extends SingleValueProcessor {

        /** Here the string size will be read. */
        final byte[] lengthBytes = new byte[2];

        @Override
        public void processValue(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            readBytes(is.in, lengthBytes);
            final int number = toNumber(lengthBytes);
            final byte[] stringBytes = readBytes(is.in, number);
            final String string = new String(stringBytes, charset);
            if (is.output == null) {
                builder.append(string);
                return;
            }
            switch (is.output) {
                case QUOTED_STRING:
                case AMBIGUOUS_INT_QSTRING:
                case AMBIGUOUS_DECIMAL_QSTRING:
                    builder.append('"');
                    builder.append(string);
                    builder.append('"');
                    break;
                default:
                case STRING:
                    builder.append(string);
                    break;
                    //throw new IllegalStateException(String.format(l10n("parser.binary.output.invalid"), is.output));
            }
        }
    }

    /**
     * Processes total keyword.
     */
    static class TotalProcessor implements ITokenProcessor {
        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            if (is.context.contains(Context.TOTAL_DECIMAL)) {
                is.output = Output.DECIMAL;
            }
        }
    }

    /**
     * Processes tokens that switches context to VALUE_INT.
     */
    static class ValueIntProcessor implements ITokenProcessor {
        @Override
        public void processToken(IronmanStream is, StringBuilder builder) throws IOException {
            is.context.push(Context.VALUE_INT);
        }
    }

    /**
     * Processes value keyword.
     */
    static class ValueProcessor implements ITokenProcessor {
        @Override
        public final void processToken(final IronmanStream is,
                final StringBuilder builder) throws IOException {
            if (is.context.contains(Context.VALUE_INT)) {
                is.output = Output.INT;
            }
        }
    }

    /**
     * Information how the token should be processed.
     */
    static class TokenInfo {

        /** Output text. Is not null. */
        public final String text;

        /** Output expected after the token. */
        public final Output output;

        /** If non-static token, processor handles further reading and converting. */
        public final ITokenProcessor processor;

        /** Flag indicating whether the token is followed by list of values.  */
        public final boolean list;

        public TokenInfo(final String text) {
            this(text, (Output) null);
        }

        public TokenInfo(final String text,
                final Output expectedOutput) {
            this(text, expectedOutput, false);
        }

        public TokenInfo(final String text,
                final Output expectedOutput, final boolean list) {
            this(text, expectedOutput, list, null);
        }

        public TokenInfo(final String text, final ITokenProcessor processor) {
            this(text, null, false, processor);
        }

        public TokenInfo(final ITokenProcessor processor) {
            this("", null, false, processor);
        }

        public TokenInfo(final String text,
                final Output expectedOutput, final boolean list,
                final ITokenProcessor processor) {
            assert text != null : "text cannot be null, use empty string instead!";
            this.text = text;
            this.output = expectedOutput;
            this.list = list;
            this.processor = processor;
        }
    }
}
