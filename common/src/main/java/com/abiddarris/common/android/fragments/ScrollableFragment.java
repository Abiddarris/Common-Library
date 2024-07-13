/***********************************************************************************
 * Copyright 2024 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************************/
package com.abiddarris.common.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

public class ScrollableFragment extends AdvanceFragment {
   
    private static final String VIEW = "view";
    private static final String SCROLLABLE_HORIZONTALY = "scrollable_horizontaly";
    private static final String SCROLLABLE_VERTICALY = "scrollable_verticaly";
    
    @Override
    @MainThread
    @CallSuper
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        return new LinearLayout(getContext());
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        
        updateVerticalScroller();
        updateHorizontalScroller();
        attachViewToScroll();
    }
    
    public void setViewToScroll(View view) {
        if(view instanceof HorizontalScrollView || view instanceof ScrollView) {
            throw new IllegalArgumentException("Cannot add " + view.getClass().getName());
        }
        saveVariable(VIEW, view);
        
        attachViewToScroll();
    }
    
    public View getViewToScroll() {
        return getVariable(VIEW);
    }
    
    public void setScrollableHorizontally(boolean scrollable) {
        saveVariable(SCROLLABLE_HORIZONTALY, scrollable);
        
        updateHorizontalScroller();
    }
    
    public boolean isScrollableHorizontally() {
        return getVariable(SCROLLABLE_HORIZONTALY, false);
    }
    
    public void setScrollableVertically(boolean scrollable) {
        saveVariable(SCROLLABLE_VERTICALY, scrollable);
        
        updateVerticalScroller();
    }
    
    public boolean isScrollableVertically() {
        return getVariable(SCROLLABLE_VERTICALY, false);
    }
    
    private void attachViewToScroll() {
        if(getView() == null) return;
        
        View view = getViewToScroll();
        if(view == null) return;
        
        LinearLayout holder = (LinearLayout)getView();
        
        ViewGroup group = findTarget(holder);
        group.removeAllViews();
        group.addView(view);
    }
    
    private ViewGroup findTarget(ViewGroup layout) {
        if(layout.getChildCount() == 0) 
            return layout;
        
        View child = layout.getChildAt(0);
        if(!(child instanceof ScrollView) && !(child instanceof HorizontalScrollView)) {
            return layout;
        }
        
        ViewGroup scroller = (ViewGroup)child;
        if(scroller.getChildCount() == 0) {
            return scroller;
        }
        
        View child2 = layout.getChildAt(0);
        if(child2 instanceof HorizontalScrollView) {
            return (ViewGroup)child2;
        }
        
        return scroller;
    }
    
    private void updateHorizontalScroller() {
        boolean scrollable = isScrollableHorizontally();
        ViewGroup holder = (ViewGroup)getView();
        if(holder == null) return;
        
        if(scrollable) {
            addHorizontalScrollView(holder);
            return;
        }
        removeHorizontalScrollView(holder);
    }
    
    private void addHorizontalScrollView(ViewGroup holder) {
        ViewGroup target = findTarget(holder);
        if(target instanceof HorizontalScrollView)
            return;
            
        ViewGroup scrollView = new HorizontalScrollView(getContext());
        if(target.getChildCount() != 0) {
            View view = target.getChildAt(0);
            target.removeAllViews();
            
            scrollView.addView(view);
        }
        target.addView(scrollView);
    }
    
    private void removeHorizontalScrollView(ViewGroup holder) {
        ViewGroup target = findTarget(holder);
        if(target instanceof LinearLayout || target instanceof ScrollView) {
            return;
        }
            
        ViewGroup parent = (ViewGroup)target.getParent();
        parent.removeAllViews();
        if(target.getChildCount() == 0) {
            return;
        }
        
        View child = target.getChildAt(0);
        target.removeAllViews();
        
        parent.addView(child);
    }
    
    private void updateVerticalScroller() {
        boolean scrollable = isScrollableVertically();
        ViewGroup holder = (ViewGroup)getView();
        if(holder == null) return;
        
        if(scrollable) {
            addVerticalScrollView(holder);
            return;
        }
        removeVerticalScrollView(holder);
    }
    
    private void addVerticalScrollView(ViewGroup holder) {
        ViewGroup target = findTarget(holder);
        if(target instanceof ScrollView)
            return;
        
        ViewGroup parent = (ViewGroup)target.getParent();
        if(target instanceof HorizontalScrollView && parent instanceof ScrollView) {
            return;
        }    
        
        ViewGroup scrollView = new ScrollView(getContext());
        if(target instanceof HorizontalScrollView) {
            ViewGroup targetParent = (ViewGroup) target.getParent();
            targetParent.removeAllViews();
            
            scrollView.addView(target);
            
            target = targetParent;
        }
        if(target instanceof LinearLayout && target.getChildCount() != 0) {
            View child = target.getChildAt(0);
            target.removeAllViews();
            
            scrollView.addView(child);
        }
        target.removeAllViews();
        target.addView(scrollView);
    }
    
    private void removeVerticalScrollView(ViewGroup holder) {
        ViewGroup target = findTarget(holder);
        
        ViewGroup parent = (ViewGroup)target.getParent();
        if(target instanceof HorizontalScrollView && parent instanceof ScrollView) {
            target = parent;
        } else if(!(target instanceof ScrollView)) {
            return;
        }
        
        parent = (ViewGroup)target.getParent();
        parent.removeAllViews();
        if(target.getChildCount() != 0) {
            View child = target.getChildAt(0);
            target.removeAllViews();
            
            parent.addView(child);
        }
    }
}
