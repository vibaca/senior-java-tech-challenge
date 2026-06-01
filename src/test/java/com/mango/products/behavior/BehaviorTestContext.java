package com.mango.products.behavior;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

@Component
@ScenarioScope
public class BehaviorTestContext {

    private UUID currentProductId;
    private UUID missingProductId;
    private MvcResult lastResult;

    public UUID getCurrentProductId() {
        return currentProductId;
    }

    public void setCurrentProductId(UUID currentProductId) {
        this.currentProductId = currentProductId;
    }

    public UUID getMissingProductId() {
        return missingProductId;
    }

    public void setMissingProductId(UUID missingProductId) {
        this.missingProductId = missingProductId;
    }

    public MvcResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(MvcResult lastResult) {
        this.lastResult = lastResult;
    }

    public void clear() {
        currentProductId = null;
        missingProductId = null;
        lastResult = null;
    }
}

