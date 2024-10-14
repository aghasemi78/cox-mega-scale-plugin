package net.runelite.client.plugins.coxmegascale;

public enum RaidStartOrder
{
    ANY("Doesn't matter"),
    FSC("FSC"),
    SCC("SCC"),
    SCF("SCF"),
    SCS("SCS"),
    SFC("SFC"),
    SPC("SPC"),
    SPS("SPS"),
    FSP("FSP"),
    SCP("SCP");

    private final String name;

    RaidStartOrder(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
