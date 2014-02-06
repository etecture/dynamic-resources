/*
 *  This file is part of the ETECTURE Open Source Community Projects.
 *
 *  Copyright (c) 2013 by:
 *
 *  ETECTURE GmbH
 *  Darmstädter Landstraße 112
 *  60598 Frankfurt
 *  Germany
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. Neither the name of the author nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.api;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public interface HttpHeaders {

    /**
     * Accept-Ranges: Welche Einheiten für Range-Angaben der Server akzeptiert.
     */
    String ACCEPT_RANGES = "Accept-Ranges";
    /**
     * Age: Wie lange das Objekt im Proxy-Cache gelegen hat.
     */
    String AGE = "Age";
    /**
     * Allow: Erlaubte Aktionen für eine bestimmte Ressource. Muss u. a. mit
     * einem 405 Method Not Allowed gesendet werden
     */
    String ALLOW = "Allow";
    /**
     * Cache-Control: Teilt allen Caching-Mechanismen entlang der Abrufkette (z.
     * B. Proxys) mit, ob und wie lange das Objekt gespeichert werden darf (in
     * sec)
     */
    String CACHE_CONTROL = "Cache-Control";
    /**
     * Connection: bevorzugte Verbindungsarten.
     */
    String CONNECTION = "Connection";
    /**
     * Content-Encoding: Codierung des Inhalts.
     */
    String CONTENT_ENCODING = "Content-Encoding";
    /**
     * Content-Language: Die Sprache, in der die Datei vorliegt (nur sinnvoll
     * bei Content-Negotiation). Wird gesendet, falls der Server mittels Content
     * Negotiation entweder eine Sprache erkannt und ausliefert oder wenn der
     * Server anhand der Endung eine Sprache erkennt.
     */
    String CONTENT_LANGUAGE = "Content-Language";
    /**
     * Content-Length: Länge des Body in Bytes.
     */
    String CONTENT_LENGTH = "Content-Length";
    /**
     * Content-Location: Alternativer Name/Speicherplatz für das angeforderte
     * Element. Wird mittels CN beispielsweise „foo.html“ angefordert, und der
     * Server schickt aufgrund des Accept-language-Felds die deutsche Version,
     * die eigentlich unter foo.html.de liegt, zurück, so wird in
     * Content-Location der Name der Originaldatei geschrieben
     */
    String CONTENT_LOCATION = "Content-Location";
    /**
     * Content-MD5: Die Base64-codierte MD5-Checksumme des Body.
     */
    String CONTENT_MD5 = "Content-MD5";
    /**
     * Content-Disposition: Mit diesem nicht standardisierten und als gefährlich
     * eingestuften Feld kann der Server für bestimmte MIME-Typen
     * Downloadfenster erzeugen und einen Dateinamen vorschlagen.
     */
    String CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * Content-Range: Welchen Bereich des Gesamtbodys der gesendete Inhalt
     * abdeckt.
     */
    String CONTENT_RANGE = "Content-Range";
    /**
     * Content-Security-Policy: Sicherheitskonzept, um Cross-Site-Scripting
     * (XSS) and ähnliche Angriffe abzuwehren.
     */
    String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    /**
     * Content-Type: Der MIME-Typ der angeforderten Datei. Er kann nicht mit
     * einer Charset Angabe im HTML header überschrieben werden.
     */
    String CONTENT_TYPE = "Content-Type";
    /**
     * Date: Zeitpunkt des Absendens.
     */
    String DATE = "Date";
    /**
     * ETag: Eine bestimmte Version einer Datei, oft als Message Digest
     * realisiert.
     */
    String ETAG = "ETag";
    /**
     * Expires: Ab wann die Datei als veraltet angesehen werden kann.
     */
    String EXPIRES = "Expires";
    /**
     * Last-Modified: Zeitpunkt der letzten Änderung an der Datei (als RFC
     * 2822).
     */
    String LAST_MODIFIED = "Last-Modified";
    /**
     * Link: Wird benutzt, um dem Client „verwandte“ Dateien oder Ressourcen
     * mitzuteilen, z. B. einen RSS-Feed, einen Favicon, Copyright-Lizenzen etc.
     * Dieses Header-Feld ist äquivalent zum <link />-Feld in (X)HTML.[4]
     */
    String LINK = "Link";
    /**
     * Location: Oft genutzt, um Clients weiterzuleiten (mit einem 3xx-Code).
     */
    String LOCATION = "Location";
    /**
     * P3P: Dieses Feld wird genutzt, um eine P3P-Datenschutz-Policy wie folgt
     * mitzuteilen:P3P:CP="your_compact_policy". P3P setzte sich nicht richtig
     * durch,[5] wird jedoch von einigen Browsern und Webseiten genutzt, um z.
     * B. Cookie-Richtlinien durchzusetzen oder zu überprüfen.
     */
    String P3P = "P3P";
    /**
     * Pragma: Implementierungs-spezifische Optionen, die mehrere Stationen in
     * der Request-Response-Kette beeinflussen können.
     */
    String PRAGMA = "Pragma";
    /**
     * Proxy-Authenticate: Anweisung, ob und wie der Client sich beim Proxy zu
     * authentifizieren hat.
     */
    String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    /**
     * Refresh: Refresh wird genutzt, um nach einer bestimmten Zahl von Sekunden
     * weiterzuleiten oder die Seite zu aktualisieren. Dieses Headerfeld ist
     * proprietär und kommt von Netscape, wird aber von den meisten Browsern
     * unterstützt
     */
    String REFRESH = "Refresh";
    /**
     * Retry-After: Falls eine Ressource zeitweise nicht verfügbar ist, so teilt
     * der Server dem Client mit diesem Feld mit, wann sich ein neuer Versuch
     * lohnt.
     */
    String RETRY_AFTER = "Retry-After";
    /**
     * Server: Serverkennung (so wie User-Agent für den Client ist, ist Server
     * für die Serversoftware).
     */
    String SERVER = "Server";
    /**
     * Set-Cookie: Ein Cookie
     */
    String SET_COOKIE = "Set-Cookie";
    /**
     * Trailer: Das Trailer-Feld enthält die Namen der Headerfelder, die im
     * Trailer der Antwort (bei Chunked-Encoding) enthalten sind. Eine Nachricht
     * in Chunked-Encoding ist aufgeteilt in den Header (Kopf), den Rumpf (Body)
     * und den Trailer, wobei der Rumpf aus Effizienzgründen in Teile (Chunks)
     * aufgeteilt sein kann. Der Trailer kann dann (je nach Wert des TE-Felders
     * der Anfrage) Header-Informationen beinhalten, deren Vorabberechnung der
     * Effizienzsteigerung zuwiderläuft.
     */
    String TRAILER = "Trailer";
    /**
     * Transfer-Encoding: Die Methode, die genutzt wird, den Inhalt sicher zum
     * Nutzer zu bringen. Zurzeit sind folgende Methoden definiert: chunked
     * (aufgeteilt), compress (komprimiert), deflate (komprimiert), gzip
     * (komprimiert), identity.
     */
    String TRANSFER_ENCODING = "Transfer-Encoding";
    /**
     * Vary: Zeigt Downstream-Proxys, wie sie anhand der Headerfelder zukünftige
     * Anfragen behandeln sollen, also ob die gecachte Antwort genutzt werden
     * kann oder eine neue Anfrage gestellt werden soll.
     */
    String VARY = "Vary";
    /**
     * Via: Informiert den Client, über welche Proxys die Antwort gesendet
     * wurde.
     */
    String VIA = "Via";
    /**
     * Warning: Eine allgemeine Warnung vor Problemen mit dem Body.
     */
    String WARNING = "Warning";
    /**
     * WWW-Authenticate: Definiert die Authentikationsmethode, die genutzt
     * werden soll, um eine bestimmte Datei herunterzuladen (Genauer definiert
     * in RFC 2617).
     */
    String WWW_AUTHENTICATE = "WWW-Authenticate";
}
