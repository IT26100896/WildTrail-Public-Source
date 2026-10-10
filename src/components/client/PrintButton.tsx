"use client";
export default function PrintButton() { return <button className="btn-dark print:hidden" onClick={() => window.print()}>Print / save PDF</button>; }

