package com.ozguryazilim.raf;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.KahveKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@Named
public class ReadOnlyModeService {
    private final Logger LOG = LoggerFactory.getLogger(ReadOnlyModeService.class);

    @Inject
    private Kahve kahve;

    private Map<ReadOnlyState, String> stateValues;

    @PostConstruct
    public void init() {
        if (stateValues == null) {
            stateValues = ReadOnlyState.getStateKeysMap().values().stream()
                    .collect(Collectors.toMap(Function.identity(), state -> kahve.get(state).getAsString()));
        }
    }

    public List<ReadOnlyState> getStateKeyList() {
        return new ArrayList<>(ReadOnlyState.getStateKeysMap().values());
    }

    public Set<Map.Entry<ReadOnlyState, String>> getStateEntrySet() {
        return this.stateValues.entrySet();
    }

    @Transactional
    public void setState(ReadOnlyState state, String value) {
        if (!isValidState(state, value)) {
            throw new IllegalArgumentException(String.format("Target value is not belonging to possible values. Possible values for %s are: %s", state.name(), Arrays.toString(state.values)));
        } else if (stateValues.get(state).equals(value)) {
            LOG.info("Target value is same as the states current value");
            return;
        } else {
            kahve.put(state, value);
            stateValues.put(state, value);
        }
    }

    public boolean isValidState(ReadOnlyState state, String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        return Arrays.stream(state.values).anyMatch(value::equals);
    }

    public String getState(ReadOnlyState state) {
        return this.stateValues.get(state);
    }

    public Boolean isEnabled() {
        return "ENABLED".equals(this.stateValues.get(ReadOnlyState.RAF_READ_ONLY));
    }

    public enum ReadOnlyState implements KahveKey {
        RAF_READ_ONLY("raf.read-only-mode", new String[] {"DISABLED", "ENABLED"}, "DISABLED");

        private static Map<String, ReadOnlyState> stateKeysMap;

        private final String key;
        private final String[] values;
        private final String defaultValue;

        ReadOnlyState(String key, String[] values, String defaultValue) {
            this.key = key;
            this.values = values;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        public String[] getValues() {
            return this.values;
        }

        @Override
        public String getDefaultValue() {
            return this.defaultValue;
        }

        public static Map<String, ReadOnlyState> getStateKeysMap() {
            return stateKeysMap;
        }

        static {
            stateKeysMap = Arrays.stream(values())
                    .collect(Collectors.toMap(ReadOnlyState::name, Function.identity()));
        }
    }

}