<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="utf-8" indent="no" />
    <xsl:template match="/">
        <book>
            <name>
                <xsl:value-of select="libro/nombre" />
            </name>
        </book>
    </xsl:template>
</xsl:stylesheet>
