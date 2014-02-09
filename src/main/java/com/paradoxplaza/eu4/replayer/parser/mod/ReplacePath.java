package com.paradoxplaza.eu4.replayer.parser.mod;

import com.paradoxplaza.eu4.replayer.ModInfo;
import com.paradoxplaza.eu4.replayer.parser.CompoundState;
import com.paradoxplaza.eu4.replayer.parser.State;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Processes replace_path = {...}.
 * @deprecated replace_path has actually format replace_path="..."
 * @see com.paradoxplaza.eu4.replayer.parser.RepeatableValueState
 */
@Deprecated
public class ReplacePath extends CompoundState<List<ModInfo>> {

    /** List of path to replace. */
    final Set<String> paths = new HashSet<>();

    /**
     * Only constructor.
     * @param parent parent state
     */
    public ReplacePath(final State<List<ModInfo>> parent) {
        super(parent);
    }

    @Override
    public State<List<ModInfo>> processWord(final List<ModInfo> context, final String word) {
        paths.add(word);
        return this;
    }
}
