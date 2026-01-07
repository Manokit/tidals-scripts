# DepositBox
All Superinterfaces:

`[ItemGroup](../../item/ItemGroup.html "interface in com.osmb.api.item")`, `[Viewable](../Viewable.html "interface in com.osmb.api.ui")`

* * *

*   Field Summary
    -------------
    
*   Method Summary
    --------------
    
    `boolean`
    
    `[close](#close\(\))()`
    
    `boolean`
    
    `[deposit](#deposit\(int,int\))(int itemID, int amount)`
    
    `boolean`
    
    `boolean`
    
    `boolean`
    
    `void`
    
    ### Methods inherited from interface com.osmb.api.item.[ItemGroup](../../item/ItemGroup.html "interface in com.osmb.api.item")
    
    `[getBoundsForSlot](about:blank/item/ItemGroup.html#getBoundsForSlot\(int\)), [getColumnGaps](about:blank/item/ItemGroup.html#getColumnGaps\(\)), [getColumnIncrement](about:blank/item/ItemGroup.html#getColumnIncrement\(int\)), [getCore](about:blank/item/ItemGroup.html#getCore\(\)), [getGroupBounds](about:blank/item/ItemGroup.html#getGroupBounds\(\)), [getGroupSize](about:blank/item/ItemGroup.html#getGroupSize\(\)), [getItemGroupBounds](about:blank/item/ItemGroup.html#getItemGroupBounds\(\)), [getItemXGap](about:blank/item/ItemGroup.html#getItemXGap\(int\)), [getItemYGap](about:blank/item/ItemGroup.html#getItemYGap\(int\)), [getMissclickBoundsForSlot](about:blank/item/ItemGroup.html#getMissclickBoundsForSlot\(int\)), [getRowGaps](about:blank/item/ItemGroup.html#getRowGaps\(\)), [getRowIncrement](about:blank/item/ItemGroup.html#getRowIncrement\(int\)), [getSlotCoordinates](about:blank/item/ItemGroup.html#getSlotCoordinates\(int\)), [getStartPoint](about:blank/item/ItemGroup.html#getStartPoint\(\)), [groupHeight](about:blank/item/ItemGroup.html#groupHeight\(\)), [groupWidth](about:blank/item/ItemGroup.html#groupWidth\(\)), [itemPaddingBottom](about:blank/item/ItemGroup.html#itemPaddingBottom\(int\)), [itemPaddingLeft](about:blank/item/ItemGroup.html#itemPaddingLeft\(int\)), [itemPaddingRight](about:blank/item/ItemGroup.html#itemPaddingRight\(int\)), [itemPaddingTop](about:blank/item/ItemGroup.html#itemPaddingTop\(int\)), [missclickPaddingBottom](about:blank/item/ItemGroup.html#missclickPaddingBottom\(int\)), [missclickPaddingLeft](about:blank/item/ItemGroup.html#missclickPaddingLeft\(int\)), [missclickPaddingRight](about:blank/item/ItemGroup.html#missclickPaddingRight\(int\)), [missclickPaddingTop](about:blank/item/ItemGroup.html#missclickPaddingTop\(int\)), [search](about:blank/item/ItemGroup.html#search\(java.util.Set\)), [xIncrement](about:blank/item/ItemGroup.html#xIncrement\(\)), [yIncrement](about:blank/item/ItemGroup.html#yIncrement\(\))`
    

*   Method Details
    --------------
    
    *   ### drawComponents
        
        void drawComponents([Canvas](../../visual/drawing/Canvas.html "class in com.osmb.api.visual.drawing") canvas)
        
    *   ### deposit
        
        boolean deposit(int itemID, int amount)
        
    *   ### depositAllIgnoreSlots
        
        boolean depositAllIgnoreSlots([Set](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Set.html "class or interface in java.util")<[Integer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html "class or interface in java.lang")\> slotsToIgnore)
        
    *   ### depositAll
        
        boolean depositAll([Set](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Set.html "class or interface in java.util")<[Integer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html "class or interface in java.lang")\> itemIDsToIgnore)
        
    *   ### depositAll
        
    *   ### close
        
        boolean close()