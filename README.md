## NoItems

Minimal Minecraft mod to add and remove any items from your modpack.

### Remove Items

This mod adds a tag `banned`. Any item with this tag will be removed in the game. To set this tag to items create a datapack with file `data/noitems/tags/item/banned.json`:

```json5
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

To add items create config file `config/noitems.json` and write json array in it:

```json5
[
    "cucumber", // create an item with default stack size = 1
    {
        "id": "cucumber", // or, set item id
        "stack": 64 // and specify this field to set stack size
    }
]
```

It creates the item with id `noitems:cucumber`, you also can find in a separate creative tab.
To set model and texture for this item create an asset pack with files: `assets/noitems/models/item/cucumber.json`, `assets/noitems/textures/item/cucumber.png` and `assets/noitems/lang/en_us.json`.

Write a content of `cucumber.json`:

```json5
{
    "parent": "minecraft:item/generated",
    "textures": {
        "layer0": "noitems:item/cucumber"
    }
}
```

And name the item in `en_us.json` file with:

```json5
{
    "item.noitems.cucumber": "Cucumber"
}
```

To pack this assets create `pack.mcmeta` file with content:

```json5
{
    "pack": {
        "description": "NoItems",
        "pack_format": 34
    }
}
```

And pack it by command `zip -r noitems.zip assets pack.mcmeta`. After, you can apply this assetpack and you'll see your item sprites at the game!
