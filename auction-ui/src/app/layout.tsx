import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "AuctionU — University Marketplace",
  description:
    "Real-time auction platform for university students. Bid, trade, and discover items within your campus community.",
  keywords: ["auction", "university", "marketplace", "bidding", "campus"],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${inter.variable} dark h-full antialiased`}>
      <body className="min-h-full flex flex-col bg-[#050505] text-slate-200 font-sans">
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  );
}
