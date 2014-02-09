package com.paradoxplaza.eu4.replayer.parser.mod;

import com.paradoxplaza.eu4.replayer.ModInfo;
import com.paradoxplaza.eu4.replayer.parser.Ignore;
import com.paradoxplaza.eu4.replayer.parser.RepeatableValueState;
import com.paradoxplaza.eu4.replayer.parser.StartAdapter;
import com.paradoxplaza.eu4.replayer.parser.State;
import com.paradoxplaza.eu4.replayer.parser.StringState;
import com.paradoxplaza.eu4.replayer.utils.Ref;
import java.util.List;

/**
 * Starting state of {@link  ModParser}.
 */
public class Start extends StartAdapter<List<ModInfo>> {

    /** Mod name. */
    final Ref<String> name = new Ref<>();

    /** Directory containing mod. */
    final Ref<String> dir = new Ref<>();

    /** Zip archive containing mod. */
    final Ref<String> archive = new Ref<>();

    /** State for processing important values. */
    final StringState<List<ModInfo>> stringState = new StringState<>(this);

    /** State for processing replace_path="...". */
    final RepeatableValueState<List<ModInfo>> replacePath = new RepeatableValueState<>(this);

    /** Ignores insignificant mod info. */
    final Ignore<List<ModInfo>> ignore = new Ignore<>(this);

    @Override
    public Start end(final List<ModInfo> context) {
        final ModInfo modInfo = new ModInfo(name.val, dir.val, archive.val, replacePath.getValues());
        context.add(modInfo);
        return this;
    }

    @Override
    public State<List<ModInfo>> processChar(final List<ModInfo>  context, final char token) {
        throw new RuntimeException(String.format(INVALID_TOKEN_EXPECTED_KEYWORD, token, "name|path|archive|replace_path"));
    }

    @Override
    public State<List<ModInfo>> processWord(final List<ModInfo> context, final String word) {
        switch (word) {
            case "name":
                return stringState.withOutput(name);
            case "path":
                return stringState.withOutput(dir);
            case "archive":
                return stringState.withOutput(archive);
            case "replace_path":
                return replacePath;
            default:
                return ignore;
        }
    }
}
