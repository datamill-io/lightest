<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/test-result">
    <xsl:variable name="test-result" select="." />
    
    <html>
      <head>
        <link href="style.css" rel="stylesheet" />
        <link href="lightest-report.css" rel="stylesheet" />
        <link href="custom-styles.css" rel="stylesheet" />
        <script type="text/javascript" src="jquery-1.11.1.min.js"></script>
        <script type="text/javascript" src="jquery.hoverIntent.js"></script>
        <script type="text/javascript" src="lightest-details.js"></script>
        <script type="text/javascript" src="custom-scripts.js"></script>
      </head>
      
      <body>
        <xsl:for-each select="info-sources/child::*">
          <xsl:call-template name="infoSourceMarkup" />
        </xsl:for-each>
        <h2>
          <span id="test-name" class="highlight-on-hover"><xsl:value-of select="@name" /></span>
        </h2>
        <xsl:for-each select="task-result">
          <table class="task-container">
            <tr>
              <td class="task-main">
                <xsl:call-template name="taskMarkup">
                  <xsl:with-param name="task-result" select="." />
                  <xsl:with-param name="test-result" select="$test-result" />
                </xsl:call-template>
              </td>
              <td class="task-spacer" />
            </tr>
          </table>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="infoSourceMarkup">
    <div class="info-source-section">
      <h3 class="info-source-title">
        <span class="highlight-on-hover"><xsl:value-of select="@title" /></span>
      </h3>
      <table class="info-source-table">
        <tr>
          <th><xsl:value-of select="@header1" /></th>
          <th><xsl:value-of select="@header2" /></th>
        </tr>
        <xsl:for-each select="child::*">
          <tr>
            <td><xsl:value-of select="name()" /></td>
            <td><xsl:value-of select="." /></td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
  </xsl:template>
  
  <xsl:template name="taskMarkup">
    <xsl:param name="task-result" />
    <xsl:param name="test-result" />
    
    <div class="task">
      <table class="task-result">
        <tr class="task-header">
          <th class="task-name"><xsl:value-of select="@name" /></th>
          <th class="task-params"><xsl:value-of select="@params" /></th>
          <th class="task-value"><xsl:value-of select="value/text()" /></th>
          <th class="task-duration"><xsl:value-of select="@duration-ms" /></th>
          <th class="task-emoticon"><xsl:value-of select="@status" /></th>
          <th class="task-display-level" />
        </tr>
        <tr class="task-info">
          <td>Description</td>
          <td colspan="5"><xsl:value-of select="@description" /></td>
        </tr>
        <tr class="task-info" style="display: none;">
          <td>Status</td>
          <td class="task-status" colspan="4"><xsl:value-of select="@status" /></td>
        </tr>
        <tr class="task-info">
          <td>Message</td>
          <td colspan="5"><xsl:value-of select="@message" /></td>
        </tr>
        <tr class="task-info">
          <td>Detailed Message</td>
          <td colspan="5">
            <xsl:choose>
              <xsl:when test="$test-result/@preformat-detailed-message = 'true'">
                <pre><xsl:value-of select="detailed-message/text()" /></pre>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="detailed-message/text()" />
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
        <xsl:if test="link">
          <tr class="task-info">
            <td>Links</td>
            <td colspan="5">
              <ul class="task-links-list">
                <xsl:for-each select="link">
                  <li>
                    <xsl:variable name="href" select="@href" />
                    <xsl:variable name="rel" select="@rel" />
                    <xsl:variable name="title" select="@title" />
                    <a href="{$href}" rel="{$rel}" title="{$title}"><xsl:value-of select="text()" /></a>
                  </li>
                </xsl:for-each>
              </ul>
            </td>
          </tr>
        </xsl:if>
      </table>
      <div class="nested-results">
        <xsl:for-each select="nested-results/task-result">
          <xsl:call-template name="taskMarkup">
            <xsl:with-param name="task-result" select="." />
            <xsl:with-param name="test-result" select="$test-result" />
          </xsl:call-template>
        </xsl:for-each>
      </div>
    </div>
  </xsl:template>
  
</xsl:stylesheet>
