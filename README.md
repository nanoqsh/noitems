## ItemLess

Minimal Minecraft mod to add and remove any items from your modpack.

### Remove Items

This mod adds a tag `banned`. Any item with this tag will be removed in the game. To set this tag to items create a datapack with file `data/itemless/tags/item`:

```json
{
    "replace": false,
    "values": [
        "minecraft:wooden_hoe", // removes wooden hoe
        "#minecraft:hoes",      // removes all hoes (by tag)
        "ilovehoes:bamboo_hoe", // removes an item from a mod
        {
            "id": "ilovehoes:bamboo_hoe",
            "required": false // optionally: removes item only if it's present
        }
    ]
}
```

### Add Items

// todo
