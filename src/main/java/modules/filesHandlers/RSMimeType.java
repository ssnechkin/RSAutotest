package modules.filesHandlers;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class RSMimeType {
    public String getMimeType(byte[] fileByte) throws IOException {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(fileByte));
        return URLConnection.guessContentTypeFromStream(is);
    }

    public String getMimeType(String fileName, byte[] fileByte) throws IOException {
        String[] artifacts = fileName.split("\\.");
        String fileNameType = "";
        if (artifacts.length > 0) {
            fileNameType = artifacts[artifacts.length - 1];
        }
        String type = getMimeType(fileByte);
        if (type == null) {
            switch (fileNameType.toUpperCase()) {
                case ".3G2":
                    return "video/3gpp2";
                case ".3GP":
                    return "video/3gpp";
                case ".3GPP":
                    return "video/3gpp";
                case ".3GPP2":
                    return "video/3gpp2";
                case "3GP":
                    return "video/3gpp";
                case "3GPP":
                    return "video/3gpp";
                case "3GPP2":
                    return "video/3gpp2";
                case "AAC":
                    return "audio/aac";
                case "ALTERNATIVE":
                    return "multipart/alternative";
                case "ATOM":
                    return "application/atom+xml";
                case "ATOM+XML":
                    return "application/atom+xml";
                case "BASIC":
                    return "audio/basic";
                case "BITTORRENT":
                    return "application/x-bittorrent";
                case "CMD":
                    return "text/cmd";
                case "CSS":
                    return "text/css";
                case "CSV":
                    return "text/csv";
                case "DTD":
                    return "application/xml-dtd";
                case "DVI":
                    return "application/x-dvi";
                case "EDI":
                    return "application/EDI-X12";
                case "EDIFACT":
                    return "application/EDIFACT";
                case "EDI-X12":
                    return "application/EDI-X12";
                case "EML":
                    return "message/rfc822";
                case "ENCRYPTED":
                    return "multipart/encrypted";
                case "EXAMPLE":
                    return "model/example";
                case "FLV":
                    return "video/x-flv";
                case "FONT":
                    return "application/font-woff";
                case "FONT-WOFF":
                    return "application/font-woff";
                case "FORM-DATA":
                    return "multipart/form-data";
                case "GIF":
                    return "image/gif";
                case "GZIP":
                    return "application/gzip";
                case "HTML":
                    return "text/html";
                case "HTTP":
                    return "message/http (RFC 2616)";
                case "HTTP (RFC 2616)":
                    return "message/http (RFC 2616)";
                case "ICO":
                    return "image/vnd.microsoft.icon";
                case "IGES":
                    return "model/iges";
                case "IGS":
                    return "model/iges";
                case "IMDN":
                    return "message/imdn+xml";
                case "IMDN+XML":
                    return "message/imdn+xml";
                case "JAVASCRIPT":
                    return "application/javascript";
                //case "JAVASCRIPT": return "text/javascript";
                case "JPEG":
                    return "image/jpeg";
                case "JQUERY":
                    return "text/x-jquery-tmpl";
                case "JSON":
                    return "application/json";
                case "KML":
                    return "application/vnd.google-earth.kml+xml";
                case "L24":
                    return "audio/L24";
                case "LATEX":
                    return "application/x-latex";
                case "MARKDOWN":
                    return "text/markdown";
                case "MESH":
                    return "model/mesh";
                case "MHT":
                    return "message/rfc822";
                case "MHTML":
                    return "message/rfc822";
                case "MIME":
                    return "message/rfc822";
                case "MIXED":
                    return "multipart/mixed";
                //case "MP4": return "audio/mp4";
                case "MP4":
                    return "video/mp4";
                //case "MPEG": return "audio/mpeg";
                case "MPEG":
                    return "video/mpeg";
                case "MSH":
                    return "model/mesh";
                case "MSWORD":
                    return "application/msword";
                case "OCTET-STREAM":
                    return "application/octet-stream";
                //case "OGG": return "application/ogg";
                //case "OGG": return "audio/ogg";
                case "OGG":
                    return "video/ogg";
                case "P12":
                    return "application/x-pkcs12";
                case "P7B":
                    return "application/x-pkcs7-certificates";
                case "P7C":
                    return "application/x-pkcs7-mime";
                case "P7M":
                    return "application/x-pkcs7-mime";
                case "P7R":
                    return "application/x-pkcs7-certreqresp";
                case "P7S":
                    return "application/x-pkcs7-signature";
                case "PARTIAL":
                    return "message/partial";
                case "PDF":
                    return "application/pdf";
                case "PFX":
                    return "application/x-pkcs12";
                case "PHP":
                    return "text/php";
                case "PJPEG":
                    return "image/pjpeg";
                case "PLAIN":
                    return "text/plain";
                case "PNG":
                    return "image/png";
                case "POSTSCRIPT":
                    return "application/postscript";
                case "QUICKTIME":
                    return "video/quicktime";
                case "RAR":
                    return "application/x-rar-compressed";
                case "RELATED":
                    return "multipart/related";
                case "RFC822":
                    return "message/rfc822";
                case "SIGNED":
                    return "multipart/signed";
                case "SILO":
                    return "model/mesh";
                case "SOAP":
                    return "application/soap+xml";
                case "SOAP+XML":
                    return "application/soap+xml";
                case "SPC":
                    return "application/x-pkcs7-certificates";
                case "STUFFIT":
                    return "application/x-stuffit";
                case "SVG":
                    return "image/svg+xml";
                case "SVG+XML":
                    return "image/svg+xml";
                case "TARBALL":
                    return "application/x-tar";
                case "TEX":
                    return "application/x-tex";
                case "TIFF":
                    return "image/tiff";
                case "VND.GOOGLE-EARTH.KML+XML":
                    return "application/vnd.google-earth.kml+xml";
                case "VND.MICROSOFT.ICON":
                    return "image/vnd.microsoft.icon";
                case "VND.MOZILLA.XUL+XML":
                    return "application/vnd.mozilla.xul+xml";
                case "VND.MS-EXCEL":
                    return "application/vnd.ms-excel";
                case "VND.MS-POWERPOINT":
                    return "application/vnd.ms-powerpoint";
                case "VND.OASIS.OPENDOCUMENT.GRAPHICS":
                    return "application/vnd.oasis.opendocument.graphics";
                case "VND.OASIS.OPENDOCUMENT.PRESENTATION":
                    return "application/vnd.oasis.opendocument.presentation";
                case "VND.OASIS.OPENDOCUMENT.SPREADSHEET":
                    return "application/vnd.oasis.opendocument.spreadsheet";
                case "VND.OASIS.OPENDOCUMENT.TEXT":
                    return "application/vnd.oasis.opendocument.text";
                case "VND.OPENXMLFORMATS-OFFICEDOCUMENT.PRESENTATIONML.PRESENTATION":
                    return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                case "VND.OPENXMLFORMATS-OFFICEDOCUMENT.SPREADSHEETML.SHEET":
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case "VND.OPENXMLFORMATS-OFFICEDOCUMENT.WORDPROCESSINGML.DOCUMENT":
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "VND.RN-REALAUDIO":
                    return "audio/vnd.rn-realaudio";
                case "VND.WAP.WBMP":
                    return "image/vnd.wap.wbmp";
                case "VND.WAVE":
                    return "audio/vnd.wave";
                case "VORBIS":
                    return "audio/vorbis";
                case "VRML":
                    return "model/vrml";
                case "WBMP":
                    return "image/vnd.wap.wbmp";
                //case "WEBM": return "audio/webm";
                case "WEBM":
                    return "video/webm";
                case "WEBP":
                    return "image/webp";
                case "WOFF":
                    return "application/font-woff";
                case "WRL":
                    return "model/vrml";
                case "X3D+BINARY":
                    return "model/x3d+binary";
                case "X3D+VRML":
                    return "model/x3d+vrml";
                case "X3D+XML":
                    return "model/x3d+xml";
                case "X-BITTORRENT ":
                    return "application/x-bittorrent";
                case "X-DVI":
                    return "application/x-dvi";
                case "X-FLV":
                    return "video/x-flv";
                case "X-FONT-TTF":
                    return "application/x-font-ttf";
                case "XHTML":
                    return "application/xhtml+xml";
                case "XHTML+XML":
                    return "application/xhtml+xml";
                case "X-JAVASCRIPT":
                    return "application/x-javascript";
                case "X-JQUERY-TMPL":
                    return "text/x-jquery-tmpl";
                case "X-LATEX":
                    return "application/x-latex";
                //case "XML": return "application/xhtml+xml";
                //case "XML": return "application/xml";
                case "XML":
                    return "text/xml";
                case "XML-DTD":
                    return "application/xml-dtd";
                case "X-MS-WAX":
                    return "audio/x-ms-wax";
                case "X-MS-WMA":
                    return "audio/x-ms-wma";
                case "X-MS-WMV":
                    return "video/x-ms-wmv";
                case "XOP":
                    return "application/xop+xml";
                case "XOP+XML":
                    return "application/xop+xml";
                case "X-PKCS12":
                    return "application/x-pkcs12";
                case "X-PKCS7-CERTIFICATES":
                    return "application/x-pkcs7-certificates";
                case "X-PKCS7-CERTREQRESP":
                    return "application/x-pkcs7-certreqresp";
                case "X-PKCS7-MIME":
                    return "application/x-pkcs7-mime";
                case "X-PKCS7-SIGNATURE":
                    return "application/x-pkcs7-signature";
                case "X-RAR-COMPRESSED":
                    return "application/x-rar-compressed";
                case "X-SHOCKWAVE-FLASH":
                    return "application/x-shockwave-flash";
                case "X-STUFFIT":
                    return "application/x-stuffit";
                case "X-TAR":
                    return "application/x-tar";
                case "X-TEX ":
                    return "application/x-tex";
                case "XUL":
                    return "application/vnd.mozilla.xul+xml";
                case "X-WWW-FORM-URLENCODED FORM ENCODED DATA[18]":
                    return "application/x-www-form-urlencoded Form Encoded Data[18]";
                case "ZIP":
                    return "application/zip";
            }
        }
        return type;
    }
}
