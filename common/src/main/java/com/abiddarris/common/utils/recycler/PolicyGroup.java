package com.abiddarris.common.utils.recycler;

import java.util.HashSet;
import java.util.Set;

class PolicyGroup {
    
    private Set<IPolicy> policies = new HashSet<>();
    private PolicyGroup parent;
    
    PolicyGroup(PolicyGroup parent) {
        this.parent = parent;
    }
    
    PolicyGroup(IPolicy... policies) {
        addPolicies(policies);
    }

    Set<IPolicy> getPolicies() {
        return policies;
    }
    
    void addPolicies(IPolicy... policies) {
        for(IPolicy policy : policies) {
            for(IPolicy internalPolicy : this.policies) {
                if(internalPolicy.getClass() == policy.getClass()) {
                    this.policies.remove(internalPolicy);
                    break;
                }
            }
            this.policies.add(policy);
        }
    }
    
    boolean hasPolicy(IPolicy policy) {
        return policies.contains(policy);
    }
    
    IPolicy findPolicy(Class clazz) {
        for(IPolicy policy : policies) {
            if(policy.getClass() == clazz) {
                return policy;
            }
        }
        return parent == null ? null : parent.findPolicy(clazz);
    }
    
}
