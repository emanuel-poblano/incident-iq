import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "IncidentIQ",
  description: "AI-powered incident investigation for engineering teams.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen font-sans antialiased">{children}</body>
    </html>
  );
}
